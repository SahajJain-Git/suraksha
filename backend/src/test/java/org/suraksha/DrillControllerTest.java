package org.suraksha;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.suraksha.dto.DrillSubmissionRequestDto;
import org.suraksha.entity.*;
import org.suraksha.repository.*;
import org.suraksha.security.CustomUserDetails;
import org.suraksha.security.JwtService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DrillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DrillScenarioRepository drillScenarioRepository;

    @Autowired
    private DrillStepRepository drillStepRepository;

    @Autowired
    private DrillChoiceRepository drillChoiceRepository;

    @Autowired
    private DrillAttemptRepository drillAttemptRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private User testStudent;
    private String jwtToken;
    private DrillScenario testScenario;
    private DrillChoice choiceSafe;
    private DrillChoice choiceDanger;

    @BeforeEach
    void setUp() {
        drillAttemptRepository.deleteAll();
        drillChoiceRepository.deleteAll();
        drillStepRepository.deleteAll();
        drillScenarioRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Setup Student User & JWT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_STUDENT", "Student role")));

        testStudent = new User("Aarav Student", "student.drill@suraksha.org", passwordEncoder.encode("Password123!"));
        testStudent.addRole(studentRole);
        testStudent = userRepository.save(testStudent);

        jwtToken = jwtService.generateToken(testStudent);

        // 2. Setup Drill Scenario
        testScenario = new DrillScenario(
                "drill-earthquake",
                "High-Rise School Earthquake Simulation",
                "EARTHQUAKE",
                "Experience simulated tremors in a 3rd floor classroom."
        );
        testScenario = drillScenarioRepository.save(testScenario);

        DrillStep step1 = new DrillStep("step-1", "Tremors shake the 3rd floor.", "What is your immediate action?", false);
        step1.setScenario(testScenario);
        step1 = drillStepRepository.save(step1);

        testScenario.setInitialStep(step1);
        testScenario = drillScenarioRepository.save(testScenario);

        choiceSafe = new DrillChoice("Drop, Cover, and Hold On under wooden desk.", "SAFE", "Excellent decision!", 50, 1);
        choiceSafe.setStep(step1);
        choiceSafe = drillChoiceRepository.save(choiceSafe);

        choiceDanger = new DrillChoice("Sprint immediately for the stairway.", "DANGER", "Running causes falls!", -30, 2);
        choiceDanger.setStep(step1);
        choiceDanger = drillChoiceRepository.save(choiceDanger);
    }

    @Test
    void testGetDrillScenarioPublic() throws Exception {
        mockMvc.perform(get("/api/drills/drill-earthquake")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.slug").value("drill-earthquake"))
                .andExpect(jsonPath("$.data.title").value("High-Rise School Earthquake Simulation"))
                .andExpect(jsonPath("$.data.steps", hasSize(1)))
                .andExpect(jsonPath("$.data.steps[0].stepKey").value("step-1"))
                .andExpect(jsonPath("$.data.steps[0].choices", hasSize(2)));
    }

    @Test
    void testGetDrillNotFound() throws Exception {
        mockMvc.perform(get("/api/drills/nonexistent-drill")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testSubmitDrillUnauthorizedWithoutToken() throws Exception {
        DrillSubmissionRequestDto request = new DrillSubmissionRequestDto(List.of(choiceSafe.getId()));

        mockMvc.perform(post("/api/drills/" + testScenario.getId() + "/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSubmitDrillSuccess() throws Exception {
        DrillSubmissionRequestDto request = new DrillSubmissionRequestDto(List.of(choiceSafe.getId()));

        mockMvc.perform(post("/api/drills/" + testScenario.getId() + "/submit")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.scenarioSlug").value("drill-earthquake"))
                .andExpect(jsonPath("$.data.totalScore").value(50))
                .andExpect(jsonPath("$.data.ratingLabel").value("Safety Conscious"))
                .andExpect(jsonPath("$.data.choiceFeedbacks", hasSize(1)))
                .andExpect(jsonPath("$.data.choiceFeedbacks[0].outcomeType").value("SAFE"));

        // Verify DrillAttempt persisted in DB
        List<DrillAttempt> attempts = drillAttemptRepository.findByUserIdOrderByCompletedAtDesc(testStudent.getId());
        assertThat(attempts).hasSize(1);
        assertThat(attempts.get(0).getTotalScore()).isEqualTo(50);
        assertThat(attempts.get(0).getRatingLabel()).isEqualTo("Safety Conscious");
    }
}
