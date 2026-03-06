package org.example.playground;

import org.example.playground.model.AttractionConfiguration;
import org.example.playground.model.AttractionType;
import org.example.playground.model.Kid;
import org.example.playground.model.PlaySite;
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

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
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
        assertThat(createdKid.getId()).isNotNull();
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
        mockMvc.perform(post("/playSites/" + updatedSite.getId() + "/kids/" + createdKid.getId()))
                .andExpect(status().isOk());

        // Verify kid is in the site
        MvcResult getSiteResult = mockMvc.perform(get("/playSites/" + updatedSite.getId()))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite siteWithKid = objectMapper.readValue(getSiteResult.getResponse().getContentAsString(), PlaySite.class);
        assertThat(siteWithKid.getKidsOnSite())
                .anyMatch(k -> k.getId().equals(createdKid.getId()));

        // 5. Remove kid from the play site
        mockMvc.perform(delete("/playSites/" + updatedSite.getId() + "/kids/" + createdKid.getId()))
                .andExpect(status().isOk());

        // Verify kid is removed
        MvcResult getSiteFinalResult = mockMvc.perform(get("/playSites/" + updatedSite.getId()))
                .andExpect(status().isOk())
                .andReturn();
        PlaySite finalSite = objectMapper.readValue(getSiteFinalResult.getResponse().getContentAsString(), PlaySite.class);
        if (finalSite.getKidsOnSite() != null) {
            assertThat(finalSite.getKidsOnSite()).noneMatch(k -> k.getId().equals(createdKid.getId()));
        }
    }
}
