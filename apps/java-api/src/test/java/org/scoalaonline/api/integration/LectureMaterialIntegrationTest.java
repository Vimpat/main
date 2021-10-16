package org.scoalaonline.api.integration;

import org.assertj.core.api.AssertionsForClassTypes;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.scoalaonline.api.model.LectureMaterial;
import org.scoalaonline.api.repository.LectureMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.scoalaonline.api.util.TestUtils.buildJsonBody;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WithMockUser(roles={"ADMIN"})
@WebAppConfiguration
public class LectureMaterialIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private LectureMaterialRepository lectureMaterialRepository;
  private LectureMaterial lectureMaterialToSave;
  private MockMvc mockMvc;
  @BeforeEach
  public void beforeTestSetup() throws Exception {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
  }

  private static Stream<Arguments> getAllCases() {

    // Create a lecture material to add to database
    ArrayList<LectureMaterial> arrayListNullCase = new ArrayList<LectureMaterial>();
    ArrayList<LectureMaterial> arrayListOneCase = new ArrayList<LectureMaterial>();
    ArrayList<LectureMaterial> arrayListManyCase = new ArrayList<LectureMaterial>();

    arrayListOneCase.add(new LectureMaterial("ID1","EXAMPLE_DOCUMENT_1.txt"));

    arrayListManyCase.add(new LectureMaterial("ID1","EXAMPLE_DOCUMENT_1.txt"));
    arrayListManyCase.add(new LectureMaterial("ID2","EXAMPLE_DOCUMENT_2.txt"));
    arrayListManyCase.add(new LectureMaterial("ID3","EXAMPLE_DOCUMENT_3.txt"));

    return Stream.of(
      Arguments.of(arrayListNullCase),
      Arguments.of(arrayListOneCase),
      Arguments.of(arrayListManyCase)
    );
  }

  private static Stream<Arguments> getByIdCases() {
    ArrayList<LectureMaterial> arrayListNullCase = new ArrayList<LectureMaterial>();
    ArrayList<LectureMaterial> arrayListOneCase = new ArrayList<LectureMaterial>();
    ArrayList<LectureMaterial> arrayListManyCase = new ArrayList<LectureMaterial>();

    arrayListOneCase.add(new LectureMaterial("ID1","EXAMPLE_DOCUMENT_1.txt"));

    arrayListManyCase.add(new LectureMaterial("ID1","EXAMPLE_DOCUMENT_1.txt"));
    arrayListManyCase.add(new LectureMaterial("ID2","EXAMPLE_DOCUMENT_2.txt"));
    arrayListManyCase.add(new LectureMaterial("ID3","EXAMPLE_DOCUMENT_3.txt"));

    return Stream.of(
      Arguments.of(arrayListNullCase,"ID1", HttpStatus.NOT_FOUND.value(), "GET: Lecture Material Not Found", null),
      Arguments.of(arrayListOneCase,"ID1",HttpStatus.OK.value(), null, arrayListOneCase.get(0)),
      Arguments.of(arrayListManyCase,"ID2", HttpStatus.OK.value(), null, arrayListManyCase.get(1))
    );
  }
  @Test
  public void givenWac_whenServletContext_thenItProvidesGreetController() {
    ServletContext servletContext = webApplicationContext.getServletContext();

    Assertions.assertNotNull(servletContext);
    Assertions.assertTrue(servletContext instanceof MockServletContext);
  }

  @ParameterizedTest
  @MethodSource("getAllCases")
  public void getAllLectureMaterialsTest(ArrayList<LectureMaterial> input) throws Exception {

    long numberOfItems = lectureMaterialRepository.count();
    lectureMaterialRepository.saveAll(input);

    MockHttpServletResponse response = this.mockMvc
      .perform(get("/lecture-materials")).andDo(print())
      .andExpect(status().isOk()).andReturn().getResponse();

    JSONArray parsedLectureMaterials = new JSONArray(response.getContentAsString()) ;

    assertThat(parsedLectureMaterials.length()).isEqualTo(numberOfItems + input.size());
    for (LectureMaterial lectureMaterial: input) {

      Optional<LectureMaterial> entity = lectureMaterialRepository.findById(lectureMaterial.getId());
      assertThat(entity).isNotNull();
      assertThat(entity.get().getDocument())
        .isEqualTo(lectureMaterial.getDocument());

      // CONTAINS TIME COMPLEXITY O(n*m) unde n = nr de litere si m aprox. egal cu n => O(n^2)
      // assertThat(response.getContentAsString().contains(lectureMaterial.getId())).isTrue();

      // TIME COMPLEXITY O(T) << O(N) unde N e numerul de entry-uri din baza de date iar T e numarul
      // de entry-uri adaugate in timpul testarii ( include entry-uri care n-au fost adaugate de teste )

      // Parcurg Lista de JSON-uri cu LectureMaterial pentru a verifica daca apar entitatile noi adaugate
      JSONObject parsedLectureMaterial = null;
      for(int i = parsedLectureMaterials.length() - 1; i >= 0; i --) {
        parsedLectureMaterial = new JSONObject(parsedLectureMaterials.get(i).toString());
        if(parsedLectureMaterial.get("id").equals(lectureMaterial.getId())){
          break;
        }
      }
      // daca gasesc, verific sa fi fost adaugat bine, daca nu, esueaza assertu cand compara id-ul
      // cu al unui alt lecture material
      assertThat(parsedLectureMaterial).isNotNull();
      assertThat(parsedLectureMaterial.get("id")).isEqualTo(lectureMaterial.getId());
      assertThat(parsedLectureMaterial.get("document")).isEqualTo(lectureMaterial.getDocument());
    }
    lectureMaterialRepository.deleteAll(input);
  }

  @ParameterizedTest
  @MethodSource("getByIdCases")
  void getLectureMaterialByIdTest(ArrayList<LectureMaterial> input, String idParam,
                                  Integer status, String errorMessage, LectureMaterial expectedLectureMaterial) throws Exception {
    lectureMaterialRepository.saveAll(input);
    //when & then
    MockHttpServletResponse response = this.mockMvc
      .perform(get("/lecture-materials/" + idParam + "/")
      .accept(MediaType.APPLICATION_JSON))
      .andReturn().getResponse();

    assertThat(response.getStatus()).isEqualTo(status);
    assertThat(response.getErrorMessage()).isEqualTo(errorMessage);

    if(errorMessage == null) {
      assertThat(response.getContentAsString()).isNotEmpty();

      JSONObject parsedLectureMaterial = new JSONObject(response.getContentAsString());
      assertThat(parsedLectureMaterial.get("id")).isEqualTo(expectedLectureMaterial.getId());
      assertThat(parsedLectureMaterial.get("document")).isEqualTo(expectedLectureMaterial.getDocument());
    }
    lectureMaterialRepository.deleteAll(input);
  }
  
  @Test
  // @ParameterizedTest(name = )
  public void addLectureMaterialTest() throws Exception {
    List<String> FieldArray = new ArrayList<String>();
    FieldArray.add("document");
    List<Object> ValuesArray = new ArrayList<Object>();
    ValuesArray.add("EXAMPLE_POST_DOCUMENT.pdf");
    StringWriter jsonObjectWriter = buildJsonBody(FieldArray, ValuesArray);

    MockHttpServletResponse response = this.mockMvc.perform(post("/lecture-materials")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonObjectWriter.toString())).andDo(print())
      .andExpect(status().isCreated()).andReturn().getResponse();

    String responseToString = response.getContentAsString();
    JSONObject parsedLectureMaterial = new JSONObject(responseToString) ;

    Optional<LectureMaterial> entity = lectureMaterialRepository.findById(parsedLectureMaterial.get("id").toString());
    assertThat(entity.get()).isNotNull();
    assertThat(entity.get().getDocument()).isEqualTo("EXAMPLE_POST_DOCUMENT.pdf");
    lectureMaterialRepository.delete(entity.get());
  }

  @Test
  void addLectureMaterialInvalidDataExceptionTest() throws Exception {

    List<Object> exceptionCases = new ArrayList<Object>();
    exceptionCases.add("");
    exceptionCases.add(null);
    for(Object exceptionCase : exceptionCases) {

      //Json Generator
      List<String> FieldArray = new ArrayList<String>();
      FieldArray.add("document");
      List<Object> ValuesArray = new ArrayList<Object>();
      ValuesArray.add(exceptionCase);
      StringWriter jsonObjectWriter = buildJsonBody(FieldArray, ValuesArray);

      //when
      MockHttpServletResponse response = mockMvc.perform(
        post("/lecture-materials").contentType(MediaType.APPLICATION_JSON)
          .content(jsonObjectWriter.toString()))
        .andReturn().getResponse();

      //in case it fails, delete the unwanted entry
      if(!response.getContentAsString().isEmpty()) {
        JSONObject parsedLectureMaterial = new JSONObject(response.getContentAsString());
        lectureMaterialRepository.deleteById(parsedLectureMaterial.get("id").toString());
      }

      //then
      AssertionsForClassTypes.assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
      AssertionsForClassTypes.assertThat(response.getContentAsString()).isEmpty();
      assertThat(response.getErrorMessage()).isEqualTo("POST: Lecture Material Invalid Document");
    }
  }
  @Test
  void updateLectureMaterialTest() throws Exception {

//    String idParam = lectureMaterialToUpdate.getId();
    //Json Generator
    List<String> FieldArray = new ArrayList<String>();
    FieldArray.add("document");
    List<Object> ValuesArray = new ArrayList<Object>();
    ValuesArray.add("Document_3.pdf");
    StringWriter jsonObjectWriter = buildJsonBody(FieldArray, ValuesArray);


    //when & then
    MockHttpServletResponse response = this.mockMvc.perform(
      patch("/lecture-materials/id0")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonObjectWriter.toString()))
      .andExpect(status().isOk())
      .andReturn().getResponse();


  }
}