package io.github.wojtekolo.hotelsystem.room;

import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceAlreadyExistsException;
import io.github.wojtekolo.hotelsystem.common.exceptions.ResourceNotFoundException;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomCreateRequest;
import io.github.wojtekolo.hotelsystem.room.dtos.RoomListItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoomController.class)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private RoomService roomService;

    @Test
    void should_return_page_of_rooms() throws Exception {
        RoomListItem item = new RoomListItem(
                null,
                "101",
                BigDecimal.valueOf(500),
                3,
                "type",
                "status",
                1
        );
        given(roomService.getRooms(any())).willReturn(new SliceImpl<>(List.of(item)));

        mockMvc.perform(get("/rooms/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("101"))
                .andExpect(jsonPath("$.content[0].pricePerNight").value(BigDecimal.valueOf(500)));

    }

    @Test
    void should_return_empty_slice_when_no_rooms() throws Exception {
        given(roomService.getRooms(any())).willReturn(new SliceImpl<>(List.of()));

        mockMvc.perform(get("/rooms/list")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());

    }

    @Test
    void should_return_conflict_when_room_exists() throws Exception {
//        given
        RoomCreateRequest createRequest = new RoomCreateRequest(
                "101",
                1,
                "",
                1L
        );
        given(roomService.addRoom(any(RoomCreateRequest.class))).willThrow(new ResourceAlreadyExistsException("Room exists"));

//        when and then
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void should_return_not_found_when_type_not_exist() throws Exception {
        RoomCreateRequest createRequest = new RoomCreateRequest(
                "101",
                1,
                "",
                1L
        );
        given(roomService.addRoom(any(RoomCreateRequest.class))).willThrow(new ResourceNotFoundException("Room type doesn't exist"));

//        when and then
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_return_bad_request_when_json_is_broken() throws Exception {
        String brokenJson = "{ \"name\": \"101\"";
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_when_invalid_data_type() throws Exception {
        String invalidTypeJson =
                """
                                {
                                    "name": "101",
                                    "floor": "Not an Integer",
                                    "description": "description",
                                    "typeId": "1"
                                }
                        """;
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidTypeJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_return_bad_request_when_invalid_data_content() throws Exception {
        RoomCreateRequest createRequest = new RoomCreateRequest(
                "",
                1,
                "",
                1L
        );
        mockMvc.perform(post("/rooms/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }
}