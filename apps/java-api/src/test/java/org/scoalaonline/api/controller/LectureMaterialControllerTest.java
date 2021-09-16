package org.scoalaonline.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.scoalaonline.api.model.LectureMaterial;
import org.scoalaonline.api.service.LectureMaterialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureJsonTesters
@AutoConfigureMockMvc
@WebMvcTest(controllers = LectureMaterialController.class)
@ActiveProfiles("test")
class LectureMaterialControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private LectureMaterialService lectureMaterialService;

  private List<LectureMaterial> lectureMaterialList;


  @BeforeEach
  void setUp() {
    this.lectureMaterialList = new ArrayList<>();
    this.lectureMaterialList.add(new LectureMaterial("id0", "Document_1.pdf"));
    this.lectureMaterialList.add(new LectureMaterial("id1", "Document_2.pdf"));
    this.lectureMaterialList.add(new LectureMaterial("id2", "Document_3.pdf"));
  }

  @Test
  void getAllLectureMaterialsTest() throws Exception{
    given(lectureMaterialService.getAll()).willReturn(lectureMaterialList);

    this.mockMvc.perform(get("/lecture-materials")
      .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.jsonPath("$",hasSize(3)))
      .andReturn();
  }

  @Test
  @Disabled
  void getLectureMaterialById() {
  }

  @Test
  @Disabled
  void addLectureMaterial() {
  }

  @Test
  @Disabled
  void updateLectureMaterial() {
  }

  @Test
  @Disabled
  void deleteLectureMaterial() {
  }
}