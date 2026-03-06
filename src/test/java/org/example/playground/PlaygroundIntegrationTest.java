package org.example.playground;

import org.example.playground.dto.ErrorResponse;
import org.example.playground.model.AttractionConfiguration;
import org.example.playground.model.AttractionType;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
import org.example.playground.persistence.KidRepository;
import org.example.playground.persistence.PlaySiteRepository;
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

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        playSiteRepository.deleteAll();
        kidRepository.deleteAll();
    }

    @Test
    public void testFullCycle() throws Exception {
        // 1. Create Kid
        Kid kid = Kid.builder()
                .name("John Doe")
                .age("5")
                .ticketNumber("T123")
                .build();

        MvcResult kidResult = mockMvc.perform(post("/kids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kid)))
                .andExpect(status().isOk())
                .andReturn();

        Kid createdKid = objectMapper.readValue(kidResult.getResponse().getContentAsString(), Kid.class);
        assertThat(createdKid.getTicketNumber()).isNotNull();
        assertThat(createdKid.getName()).isEqualTo("John Doe");

        // 2. Create default PlaySite
        MvcResult siteResult = mockMvc.perform(post("/playSites")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        PlaySite createdSite = objectMapper.readValue(siteResult.getResponse().getContentAsString(), PlaySite.class);
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

        MvcResult updateResult = mockMvc.perform(put("/playSites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdSite)))
                .andExpect(status().isOk())
                .andReturn();

        PlaySite updatedSite = objectMapper.readValue(updateResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(updatedSite.getAttractions())
                .isNotNull()
                .hasSize(1);
        assertThat(updatedSite.getAttractions().getFirst().getAttractionType())
                .isEqualTo(AttractionType.SLIDE);

        // 4. Add the kid to play site
        mockMvc.perform(post("/playSites/" + updatedSite.getId() + "/kids/" + createdKid.getTicketNumber()))
                .andExpect(status().isOk());

        // Verify kid is in the site
        MvcResult getSiteResult = mockMvc.perform(get("/playSites/" + updatedSite.getId()))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite siteWithKid = objectMapper.readValue(getSiteResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(siteWithKid.getKidsOnSite())
                .anyMatch(k -> k.getTicketNumber().equals(createdKid.getTicketNumber()));

        // 5. Remove kid from the play site
        mockMvc.perform(delete("/playSites/" + updatedSite.getId() + "/kids/" + createdKid.getTicketNumber()))
                .andExpect(status().isOk());

        // Verify kid is removed
        MvcResult getSiteFinalResult = mockMvc.perform(get("/playSites/" + updatedSite.getId()))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite finalSite = objectMapper.readValue(getSiteFinalResult.getResponse().getContentAsString(), PlaySite.class);
        if (finalSite.getKidsOnSite() != null) {
            assertThat(finalSite.getKidsOnSite()).noneMatch(k -> k.getTicketNumber().equals(createdKid.getTicketNumber()));
        }
    }

    @Test
    public void testCapacityAndQueue() throws Exception {
        // 1. Create a PlaySite with capacity 1
        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.CAROUSEL) // capacity 1
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder()
                .attractions(Collections.singletonList(attraction))
                .build();

        MvcResult siteResult = mockMvc.perform(post("/playSites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(site)))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite createdSite = objectMapper.readValue(siteResult.getResponse().getContentAsString(), PlaySite.class);

        // 2. Create 2 kids
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("T1").acceptWaiting(true).build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("T2").acceptWaiting(true).build();

        MvcResult kid1Result = mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid1)))
                .andExpect(status().isOk()).andReturn();
        kid1 = objectMapper.readValue(kid1Result.getResponse().getContentAsString(), Kid.class);

        MvcResult kid2Result = mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid2)))
                .andExpect(status().isOk()).andReturn();
        kid2 = objectMapper.readValue(kid2Result.getResponse().getContentAsString(), Kid.class);

        // 3. Add kid 1 to the site (should succeed)
        mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid1.getTicketNumber()))
                .andExpect(status().isOk());

        // 4. Add kid 2 to the site (should go to queue)
        mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid2.getTicketNumber()))
                .andExpect(status().isOk());

        // 5. Verify kid 1 is on site and kid 2 is in queue
        MvcResult getSiteResult = mockMvc.perform(get("/playSites/" + createdSite.getId()))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite siteStatus = objectMapper.readValue(getSiteResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(siteStatus.getKidsOnSite()).hasSize(1);
        assertThat(siteStatus.getKidsOnSite().getFirst().getTicketNumber()).isEqualTo(kid1.getTicketNumber());
        assertThat(siteStatus.getKidsQueue()).hasSize(1);
        assertThat(siteStatus.getKidsQueue().getFirst().getTicketNumber()).isEqualTo(kid2.getTicketNumber());

        // 6. Remove kid 1 (kid 2 should move to site)
        mockMvc.perform(delete("/playSites/" + createdSite.getId() + "/kids/" + kid1.getTicketNumber()))
                .andExpect(status().isOk());

        // 7. Verify kid 2 is now on site and the queue is empty
        MvcResult getSiteFinalResult = mockMvc.perform(get("/playSites/" + createdSite.getId()))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite finalSiteStatus = objectMapper.readValue(getSiteFinalResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(finalSiteStatus.getKidsOnSite()).hasSize(1);
        assertThat(finalSiteStatus.getKidsOnSite().getFirst().getTicketNumber()).isEqualTo(kid2.getTicketNumber());
        assertThat(finalSiteStatus.getKidsQueue()).isEmpty();
    }

    @Test
    public void testUtilization() throws Exception {
        // 1. Create a PlaySite with capacity 10 (Ball Pit)
        AttractionConfiguration attraction = AttractionConfiguration.builder()
                .attractionType(AttractionType.BALL_PIT) // capacity 10
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder()
                .attractions(Collections.singletonList(attraction))
                .build();

        MvcResult siteResult = mockMvc.perform(post("/playSites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(site)))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite createdSite = objectMapper.readValue(siteResult.getResponse().getContentAsString(), PlaySite.class);

        // 2. Check initial utilization (should be 0.0)
        mockMvc.perform(get("/playSites/" + createdSite.getId() + "/utilization"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEqualTo("0.0"));

        // 3. Add 5 kids
        for (int i = 0; i < 5; i++) {
            Kid kid = Kid.builder().name("Kid " + i).ticketNumber("TU" + i).build();
            MvcResult kidResult = mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid)))
                    .andExpect(status().isOk()).andReturn();
            kid = objectMapper.readValue(kidResult.getResponse().getContentAsString(), Kid.class);
            mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid.getTicketNumber()))
                    .andExpect(status().isOk());
        }

        // 4. Check utilization (should be 50.0)
        mockMvc.perform(get("/playSites/" + createdSite.getId() + "/utilization"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentAsString()).isEqualTo("50.0"));
    }

    @Test
    public void testQueueMovesWhenCapacityIncreases() throws Exception {
        // 1. Create a PlaySite with capacity 1
        AttractionConfiguration attraction1 = AttractionConfiguration.builder()
                .attractionType(AttractionType.CAROUSEL) // capacity 1
                .quantity(1)
                .build();
        PlaySite site = PlaySite.builder()
                .attractions(Collections.singletonList(attraction1))
                .build();

        MvcResult siteResult = mockMvc.perform(post("/playSites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(site)))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite createdSite = objectMapper.readValue(siteResult.getResponse().getContentAsString(), PlaySite.class);

        // 2. Create 2 kids (accepting waiting)
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("TK1").acceptWaiting(true).build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("TK2").acceptWaiting(true).build();

        MvcResult kid1Result = mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid1)))
                .andExpect(status().isOk()).andReturn();
        kid1 = objectMapper.readValue(kid1Result.getResponse().getContentAsString(), Kid.class);

        MvcResult kid2Result = mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid2)))
                .andExpect(status().isOk()).andReturn();
        kid2 = objectMapper.readValue(kid2Result.getResponse().getContentAsString(), Kid.class);

        // 3. Add kid 1 to site and kid 2 to queue
        mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid1.getTicketNumber())).andExpect(status().isOk());
        mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid2.getTicketNumber())).andExpect(status().isOk());

        // Verify state
        MvcResult midResult = mockMvc.perform(get("/playSites/" + createdSite.getId())).andExpect(status().isOk()).andReturn();
        PlaySite midSite = objectMapper.readValue(midResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(midSite.getKidsOnSite()).hasSize(1);
        assertThat(midSite.getKidsQueue()).hasSize(1);

        // 4. Increase capacity by adding another carousel
        AttractionConfiguration attraction2 = AttractionConfiguration.builder()
                .attractionType(AttractionType.CAROUSEL)
                .quantity(1)
                .build();
        // We need to keep the original attraction ID if we want to update it, but here we're replacing the list.
        // Actually, let's just update the quantity of the existing attraction.
        midSite.getAttractions().getFirst().setQuantity(2);

        mockMvc.perform(put("/playSites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(midSite)))
                .andExpect(status().isOk());

        // 5. Verify kid 2 moved to site automatically
        MvcResult finalResult = mockMvc.perform(get("/playSites/" + createdSite.getId())).andExpect(status().isOk()).andReturn();
        PlaySite finalSite = objectMapper.readValue(finalResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(finalSite.getKidsOnSite()).hasSize(2);
        assertThat(finalSite.getKidsQueue()).isEmpty();
    }

    @Test
    public void testTicketNumberUniqueness() throws Exception {
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("UNIQUE_TICKET").build();
        mockMvc.perform(post("/kids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kid1)))
                .andExpect(status().isOk());

        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("UNIQUE_TICKET").build();
        MvcResult result = mockMvc.perform(post("/kids")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(kid2)))
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
        MvcResult siteResult = mockMvc.perform(post("/playSites").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(site)))
                .andExpect(status().isOk()).andReturn();
        PlaySite createdSite = objectMapper.readValue(siteResult.getResponse().getContentAsString(), PlaySite.class);

        // 2. Create 2 kids, kid2 does not accept waiting
        Kid kid1 = Kid.builder().name("Kid 1").ticketNumber("T1").acceptWaiting(true).build();
        Kid kid2 = Kid.builder().name("Kid 2").ticketNumber("T2").acceptWaiting(false).build();

        mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid1))).andExpect(status().isOk());
        mockMvc.perform(post("/kids").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(kid2))).andExpect(status().isOk());

        // 3. Add kid1 (should succeed)
        mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid1.getTicketNumber())).andExpect(status().isOk());

        // 4. Add kid2 (should fail with statusCode = 409)
        MvcResult result = mockMvc.perform(post("/playSites/" + createdSite.getId() + "/kids/" + kid2.getTicketNumber()))
                .andExpect(status().isConflict())
                .andReturn();

        ErrorResponse error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
        assertThat(error.getErrorMessage()).isEqualTo("Site is full and kid does not accept waiting");
    }
}
