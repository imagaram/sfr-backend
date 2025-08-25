package com.sfr.tokyo.sfr_backend.controller;

import com.sfr.tokyo.sfr_backend.entity.CharacterLifecycle;
import com.sfr.tokyo.sfr_backend.entity.CharacterStatus;
import com.sfr.tokyo.sfr_backend.repository.CharacterRepository;
import com.sfr.tokyo.sfr_backend.service.ImageUploadService;
import com.sfr.tokyo.sfr_backend.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put; // not used
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("removal")
@WebMvcTest(CharacterController.class)
@AutoConfigureMockMvc(addFilters = false)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper は未使用のため削除

    @MockBean
    private CharacterRepository characterRepository;

    @MockBean
    private ImageUploadService imageUploadService;

    @MockBean
    private com.sfr.tokyo.sfr_backend.service.JwtService jwtService;

    @MockBean
    private com.sfr.tokyo.sfr_backend.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    // RateLimitConfig が WebMvc スライスに読み込まれるため、依存のサービスをモック
    @MockBean
    private com.sfr.tokyo.sfr_backend.service.RateLimitService rateLimitService;

    @BeforeEach
    void allowRateLimit() {
        org.mockito.Mockito.when(rateLimitService.isAllowed(org.mockito.ArgumentMatchers.anyString())).thenReturn(true);
        org.mockito.Mockito.when(rateLimitService.isAuthAllowed(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(true);
        org.mockito.Mockito.when(rateLimitService.getRemainingRequests(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(100);
        org.mockito.Mockito.when(rateLimitService.getRemainingAuthRequests(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(5);
        org.mockito.Mockito.when(rateLimitService.getSecondsUntilReset(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(0L);
    }

    private User buildUser() {
        UUID id = UUID.randomUUID();
        return User.builder().id(id).build();
    }

    @Test
    void createCharacter_shouldReturnCreated() throws Exception {
        User user = buildUser();
        MockMultipartFile file = new MockMultipartFile("imageFile", "img.png", MediaType.IMAGE_PNG_VALUE,
                "data".getBytes());

        when(imageUploadService.uploadImage(any())).thenReturn("uploaded.png");

        CharacterLifecycle saved = CharacterLifecycle.builder()
                .id(1L)
                .name("C1")
                .profile("p")
                .imageUrl("uploaded.png")
                .user(user)
                .status(CharacterStatus.ACTIVE)
                .build();
        when(characterRepository.findByNameAndUser_Id(eq("C1"), eq(user.getId()))).thenReturn(null);
        when(characterRepository.save(any())).thenReturn(saved);

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(user, null);

        mockMvc.perform(multipart("/api/characters")
                .file(file)
                .param("name", "C1")
                .param("profile", "p")
                .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("C1"))
                .andExpect(jsonPath("$.userId").value(user.getId().toString()));

        verify(imageUploadService).uploadImage(any());
        verify(characterRepository).save(any());
    }

    @Test
    void getAllCharacters_shouldReturnList() throws Exception {
        User user = buildUser();
        CharacterLifecycle c = CharacterLifecycle.builder().id(1L).name("C1").user(user).build();
        when(characterRepository.findByUser_Id(user.getId())).thenReturn(Collections.singletonList(c));

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(user, null);

        mockMvc.perform(get("/api/characters").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("C1"));
    }

    @Test
    void getCharacterById_notFound_shouldReturn404() throws Exception {
        User user = buildUser();
        when(characterRepository.findById(99L)).thenReturn(Optional.empty());
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(user, null);

        mockMvc.perform(get("/api/characters/99").principal(principal))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCharacter_shouldReturnOk() throws Exception {
        User user = buildUser();
        CharacterLifecycle existing = CharacterLifecycle.builder().id(1L).name("Old").user(user).build();
        when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(characterRepository.findByNameAndUser_Id("New", user.getId())).thenReturn(null);
        when(imageUploadService.uploadImage(any())).thenReturn("new.png");
        when(characterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("imageFile", "img.png", MediaType.IMAGE_PNG_VALUE,
                "data".getBytes());
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(user, null);

        // multipart builder defaults to POST; override to PUT
        mockMvc.perform(multipart("/api/characters/1")
                .file(file)
                .param("name", "New")
                .param("profile", "p")
                .principal(principal)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void deleteCharacter_shouldReturnNoContent() throws Exception {
        User user = buildUser();
        CharacterLifecycle existing = CharacterLifecycle.builder().id(1L).user(user).build();
        when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));

        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(user, null);

        mockMvc.perform(delete("/api/characters/1").principal(principal))
                .andExpect(status().isNoContent());
    }
}
