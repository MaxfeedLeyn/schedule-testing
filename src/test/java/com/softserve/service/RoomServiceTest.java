package com.softserve.service;

import com.softserve.dto.RoomDTO;
import com.softserve.entity.Room;
import com.softserve.entity.RoomType;
import com.softserve.exception.EntityAlreadyExistsException;
import com.softserve.exception.EntityNotFoundException;
import com.softserve.exception.SortOrderNotExistsException;
import com.softserve.mapper.RoomForScheduleInfoMapper;
import com.softserve.mapper.RoomMapper;
import com.softserve.repository.RoomRepository;
import com.softserve.repository.SortOrderRepository;
import com.softserve.service.impl.RoomServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomMapper roomMapper;
    @Mock
    private RoomForScheduleInfoMapper roomForScheduleInfoMapper;
    @Mock
    private SortOrderRepository<Room> sortOrderRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room room;
    private RoomDTO roomDTO;

    @BeforeEach
    void setUp() {
        RoomType roomType = new RoomType();
        roomType.setId(1L);
        roomType.setDescription("Small auditory");

        room = new Room();
        room.setId(1L);
        room.setName("1 Room");
        room.setType(roomType);

        roomDTO = new RoomDTO();
        roomDTO.setId(1L);
        roomDTO.setName("1 Room");
    }

    @Test
    void getById() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(roomMapper.convertToDto(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roomDTO.getId());
        assertThat(result.getName()).isEqualTo(roomDTO.getName());
        verify(roomRepository, times(1)).findById(1L);
        verify(roomMapper, times(1)).convertToDto(room);
    }

    @Test
    void throwEntityNotFoundExceptionIfRoomNotFound() {
        when(roomRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> roomService.getById(2L));

        verify(roomRepository, times(1)).findById(2L);
    }

    @Test
    void saveRoomIfNameAndTypeAreNotExist() {
        when(roomMapper.convertToEntity(roomDTO)).thenReturn(room);
        when(roomRepository.countRoomDuplicates(room)).thenReturn(0L);
        when(roomRepository.getLastSortOrder()).thenReturn(Optional.of(0));
        when(roomRepository.save(room)).thenReturn(room);
        when(roomMapper.convertToDto(room)).thenReturn(roomDTO);

        RoomDTO result = roomService.save(roomDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(roomDTO.getName());
        verify(roomRepository, times(1)).save(room);
        verify(roomRepository, times(1)).countRoomDuplicates(room);
    }

    @Test
    void throwEntityAlreadyExistsExceptionIfSavedRoomAlreadyExists() {
        when(roomMapper.convertToEntity(roomDTO)).thenReturn(room);
        when(roomRepository.countRoomDuplicates(room)).thenReturn(1L);

        assertThrows(EntityAlreadyExistsException.class, () -> roomService.save(roomDTO));

        verify(roomRepository, never()).save(any());
        verify(roomRepository, times(1)).countRoomDuplicates(room);
    }

    @Test
    void updateRoomIfNameAndTypeAreNotExist() {
        RoomDTO updatedRoomDTO = new RoomDTO();
        updatedRoomDTO.setId(1L);
        updatedRoomDTO.setName("1 Room updated");

        Room updatedRoom = new Room();
        updatedRoom.setId(1L);
        updatedRoom.setName("1 Room updated");

        when(roomMapper.convertToEntity(updatedRoomDTO)).thenReturn(updatedRoom);
        when(roomRepository.countRoomDuplicates(updatedRoom)).thenReturn(0L);
        when(sortOrderRepository.getSortOrderById(1L)).thenReturn(Optional.of(1));
        when(roomRepository.update(updatedRoom)).thenReturn(updatedRoom);
        when(roomMapper.convertToDto(updatedRoom)).thenReturn(updatedRoomDTO);

        RoomDTO result = roomService.update(updatedRoomDTO);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(updatedRoomDTO);
        verify(roomRepository, times(1)).update(updatedRoom);
        verify(roomRepository, times(1)).countRoomDuplicates(updatedRoom);
    }

    @Test
    void throwEntityAlreadyExistsExceptionIfUpdatedNameAndTypeAlreadyExist() {
        RoomDTO updatedRoomDTO = new RoomDTO();
        updatedRoomDTO.setId(1L);
        updatedRoomDTO.setName("1 Room updated");

        Room updatedRoom = new Room();
        updatedRoom.setId(1L);
        updatedRoom.setName("1 Room updated");

        when(roomMapper.convertToEntity(updatedRoomDTO)).thenReturn(updatedRoom);
        when(roomRepository.countRoomDuplicates(updatedRoom)).thenReturn(1L);

        assertThrows(EntityAlreadyExistsException.class, () -> roomService.update(updatedRoomDTO));

        verify(roomRepository, never()).update(any());
        verify(roomRepository, times(1)).countRoomDuplicates(updatedRoom);
    }

    @Test
    void throwSortOrderNotExistsExceptionWhenCreateAfterNotExistRoom() {
        Long afterNotExistRoomId = 10L;

        when(roomMapper.convertToEntity(roomDTO)).thenReturn(room);
        when(sortOrderRepository.createAfterOrder(room, afterNotExistRoomId))
                .thenThrow(SortOrderNotExistsException.class);

        assertThrows(SortOrderNotExistsException.class,
                () -> roomService.createAfterOrder(roomDTO, afterNotExistRoomId));

        verify(sortOrderRepository, times(1)).createAfterOrder(room, afterNotExistRoomId);
    }

    @Test
    void createAfterOrderSuccessfully() {
        RoomDTO roomWithOrderDTO = new RoomDTO();
        roomWithOrderDTO.setName("11 Room");

        Room roomWithOrder = new Room();
        roomWithOrder.setName("11 Room");
        roomWithOrder.setSortOrder(1);

        Long afterId = 0L;

        when(roomMapper.convertToEntity(roomDTO)).thenReturn(room);
        when(sortOrderRepository.createAfterOrder(room, afterId)).thenReturn(roomWithOrder);
        when(roomMapper.convertToDto(roomWithOrder)).thenReturn(roomWithOrderDTO);

        RoomDTO result = roomService.createAfterOrder(roomDTO, afterId);

        assertThat(result).isNotNull();
        assertThat(result).usingRecursiveComparison().isEqualTo(roomWithOrderDTO);
        verify(sortOrderRepository, times(1)).createAfterOrder(room, afterId);
    }
}
