package org.example.playground;

import org.example.playground.error.ErrorResponse;
import org.example.playground.model.AttractionConfiguration;
import org.example.playground.model.AttractionType;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.KidRepository;
import org.example.playground.persistence.PlaySiteRepository;
import org.example.playground.persistence.VisitorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class PlaygroundIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KidRepository kidRepository;

    @Autowired
    private PlaySiteRepository playSiteRepository;

    @Autowired
    private VisitorRepository visitorRepository;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        playSiteRepository.deleteAll();
        kidRepository.deleteAll();
        visitorRepository.deleteAll();
    }

    @Test
    public void testFullCycle() {
        // 1. Create Kid
        Kid kid = Kid.builder()
                .name("John Doe")
                .age("5")
                .ticketNumber("T123")
                .build();

        Kid createdKid = addKid(kid);
        assertThat(createdKid.getTicketNumber()).isNotNull();
        assertThat(createdKid.getName()).isEqualTo("John Doe");

        // 2. Create default PlaySite
        PlaySite createdSite = createPlaySite(null);
        assertThat(createdSite.getId()).isNotNull();
        assertThat(createdSite.getKidsOnSite())
                .isNotNull()
                .isEmpty();
        assertThat(createdSite.getAttractions())
                .isNotNull()
                .isEmpty();
        assertThat(createdSite.getKidsQueue())
                .isNotNull()
                .isEmpty();

        // 3. Update the play site (add an attraction)
        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.SLIDE)
                .quantity(1)
                .build();
        createdSite.setAttractions(Collections.singletonList(attraction));

        PlaySite updatedSite = updatePlaySite(createdSite);
        assertThat(updatedSite.getAttractions())
                .isNotNull()
                .hasSize(1);
        assertThat(updatedSite.getAttractions().getFirst().getAttractionType())
                .isEqualTo(AttractionType.SLIDE);

        // 4. Add the kid to play site
        addKidToSite(updatedSite.getId(), createdKid.getTicketNumber());

        // Verify kid is in the site
        PlaySite siteWithKid = getPlaySite(updatedSite.getId());
        assertThat(siteWithKid.getKidsOnSite())
                .anyMatch(k -> k.getTicketNumber().equals(createdKid.getTicketNumber()));

        // 5. Remove kid from the play site
        removeKidFromSite(updatedSite.getId(), createdKid.getTicketNumber());

        // Verify kid is removed
        PlaySite finalSite = getPlaySite(updatedSite.getId());
        if (finalSite.getKidsOnSite() != null) {
            assertThat(finalSite.getKidsOnSite()).noneMatch(k -> k.getTicketNumber().equals(createdKid.getTicketNumber()));
        }
    }

    @Test
    public void testCapacityAndQueue() {
        // 1. Create a PlaySite with capacity 1
        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.CAROUSEL) // capacity 1
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder()
                .attractions(Collections.singletonList(attraction))
                .build();

        PlaySite createdSite = createPlaySite(site);

        // 2. Create 2 kids
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("T1").acceptWaiting(true).build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("T2").acceptWaiting(true).build();

        kid1 = addKid(kid1);
        kid2 = addKid(kid2);

        // 3. Add kid 1 to the site (should succeed)
        addKidToSite(createdSite.getId(), kid1.getTicketNumber());

        // 4. Add kid 2 to the site (should go to queue)
        addKidToSite(createdSite.getId(), kid2.getTicketNumber());

        // 5. Verify kid 1 is on site and kid 2 is in queue
        PlaySite siteStatus = getPlaySite(createdSite.getId());
        assertThat(siteStatus.getKidsOnSite()).hasSize(1);
        assertThat(siteStatus.getKidsOnSite().getFirst().getTicketNumber()).isEqualTo(kid1.getTicketNumber());
        assertThat(siteStatus.getKidsQueue()).hasSize(1);
        assertThat(siteStatus.getKidsQueue().getFirst().getTicketNumber()).isEqualTo(kid2.getTicketNumber());

        // 6. Remove kid 1 (kid 2 should move to site)
        removeKidFromSite(createdSite.getId(), kid1.getTicketNumber());

        // 7. Verify kid 2 is now on site and the queue is empty
        PlaySite finalSiteStatus = getPlaySite(createdSite.getId());
        assertThat(finalSiteStatus.getKidsOnSite()).hasSize(1);
        assertThat(finalSiteStatus.getKidsOnSite().getFirst().getTicketNumber()).isEqualTo(kid2.getTicketNumber());
        assertThat(finalSiteStatus.getKidsQueue()).isEmpty();
    }

    @Test
    public void testUtilization() {
        // 1. Create a PlaySite with capacity 10 (Ball Pit)
        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.BALL_PIT) // capacity 10
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder()
                .attractions(Collections.singletonList(attraction))
                .build();

        PlaySite createdSite = createPlaySite(site);

        // 2. Check initial utilization (should be 0.0)
        assertThat(getUtilization(createdSite.getId())).isEqualTo("0.0");

        // 3. Add 5 kids
        for (int i = 0; i < 5; i++) {
            Kid kid = Kid.builder().name("Kid " + i).ticketNumber("TU" + i).build();
            kid = addKid(kid);
            addKidToSite(createdSite.getId(), kid.getTicketNumber());
        }

        // 4. Check utilization (should be 50.0)
        assertThat(getUtilization(createdSite.getId())).isEqualTo("50.0");
    }

    @Test
    public void testQueueMovesWhenCapacityIncreases() {
        // 1. Create a PlaySite with capacity 1
        AttractionConfiguration attraction1 = AttractionConfiguration.builder()
                .attractionType(AttractionType.CAROUSEL) // capacity 1
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder()
                .attractions(Collections.singletonList(attraction1))
                .build();

        PlaySite createdSite = createPlaySite(site);

        // 2. Create 2 kids (accepting waiting)
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("TK1").acceptWaiting(true).build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("TK2").acceptWaiting(true).build();

        kid1 = addKid(kid1);
        kid2 = addKid(kid2);

        // 3. Add kid 1 to site and kid 2 to queue
        addKidToSite(createdSite.getId(), kid1.getTicketNumber());
        addKidToSite(createdSite.getId(), kid2.getTicketNumber());

        // Verify state
        PlaySite midSite = getPlaySite(createdSite.getId());
        assertThat(midSite.getKidsOnSite()).hasSize(1);
        assertThat(midSite.getKidsQueue()).hasSize(1);

        // 4. Increase capacity by adding another carousel
        midSite.getAttractions().getFirst().setQuantity(2);

        updatePlaySite(midSite);

        // 5. Verify kid 2 moved to site automatically
        PlaySite finalSite = getPlaySite(createdSite.getId());
        assertThat(finalSite.getKidsOnSite()).hasSize(2);
        assertThat(finalSite.getKidsQueue()).isEmpty();
    }

    @Test
    public void testTicketNumberUniqueness() throws Exception {
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("UNIQUE_TICKET").build();
        addKid(kid1);

        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("UNIQUE_TICKET").build();
        MvcResult result = mockMvc.perform(post("/kids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(kid2)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(error.getErrorMessage()).contains("Ticket number already exists");
    }

    @Test
    public void testKidDoesNotAcceptWaiting() throws Exception {
        // 1. Create a PlaySite with capacity 1
        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.CAROUSEL)
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder().attractions(Collections.singletonList(attraction)).build();
        PlaySite createdSite = createPlaySite(site);

        // 2. Create 2 kids, kid2 does not accept waiting
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("T1").acceptWaiting(true).build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("T2").acceptWaiting(false).build();

        addKid(kid1);
        addKid(kid2);

        // 3. Add kid1 (should succeed)
        addKidToSite(createdSite.getId(), kid1.getTicketNumber());

        // 4. Add kid2 (should fail with statusCode = 409)
        MvcResult result = mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid2.getTicketNumber()))
                .andExpect(status().isConflict())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(error.getErrorMessage()).isEqualTo("Site is full and kid does not accept waiting");
    }

    @Test
    public void testVisitorCount() {
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("T1").build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("T2").build();

        addKid(kid1);
        addKid(kid2);

        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.BALL_PIT)
                .quantity(1)
                .build();
        PlaySite siteTemplate = PlaySite.builder().attractions(Collections.singletonList(attraction)).build();

        PlaySite site1 = createPlaySite(siteTemplate);
        PlaySite site2 = createPlaySite(siteTemplate);

        addKidToSite(site1.getId(), "T1");
        addKidToSite(site2.getId(), "T2");

        assertThat(getVisitorCount()).isEqualTo(2);

        removeKidFromSite(site1.getId(), "T1");
        addKidToSite(site2.getId(), "T1");

        assertThat(getVisitorCount()).isEqualTo(2);
    }

    @Test
    public void testGetKidNotFound() throws Exception {
        String nonExistentTicket = "NON_EXISTENT";
        MvcResult result = mockMvc.perform(get("/kids/" + nonExistentTicket))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(error.getErrorMessage()).isEqualTo("Kid with ticket number " + nonExistentTicket + " not found");
    }

    @Test
    public void testGetPlaySiteNotFound() throws Exception {
        Long nonExistentId = 999L;
        MvcResult result = mockMvc.perform(get("/playSites/" + nonExistentId))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(error.getErrorMessage()).isEqualTo("PlaySite with id " + nonExistentId + " not found");
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Kid addKid(Kid kid) {
        try {
            MvcResult kidResult = mockMvc.perform(post("/kids")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(kid)))
                    .andExpect(status().isOk())
                    .andReturn();
            return objectMapper.readValue(kidResult.getResponse().getContentAsString(), Kid.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PlaySite createPlaySite(PlaySite site) {
        try {
            MvcResult siteResult = mockMvc.perform(post("/playSites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(site == null ? "" : toJson(site)))
                    .andExpect(status().isOk())
                    .andReturn();
            return objectMapper.readValue(siteResult.getResponse().getContentAsString(), PlaySite.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PlaySite updatePlaySite(PlaySite site) {
        try {
            MvcResult updateResult = mockMvc.perform(put("/playSites")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(site)))
                    .andExpect(status().isOk())
                    .andReturn();
            return objectMapper.readValue(updateResult.getResponse().getContentAsString(), PlaySite.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PlaySite getPlaySite(Long siteId) {
        try {
            MvcResult getSiteResult = mockMvc.perform(get("/playSites/" + siteId))
                    .andExpect(status().isOk())
                    .andReturn();
            return objectMapper.readValue(getSiteResult.getResponse().getContentAsString(), PlaySite.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addKidToSite(Long siteId, String ticketNumber) {
        try {
            mockMvc.perform(post("/playSites/" + siteId + "/kids/" + ticketNumber))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void removeKidFromSite(Long siteId, String ticketNumber) {
        try {
            mockMvc.perform(delete("/playSites/" + siteId + "/kids/" + ticketNumber))
                    .andExpect(status().isOk());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getUtilization(Long siteId) {
        try {
            MvcResult result = mockMvc.perform(get("/playSites/" + siteId + "/utilization"))
                    .andExpect(status().isOk())
                    .andReturn();
            return result.getResponse().getContentAsString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long getVisitorCount() {
        try {
            MvcResult result = mockMvc.perform(get("/visitors/count"))
                    .andExpect(status().isOk())
                    .andReturn();
            return Long.parseLong(result.getResponse().getContentAsString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
