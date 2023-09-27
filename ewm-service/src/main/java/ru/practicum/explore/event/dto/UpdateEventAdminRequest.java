package ru.practicum.explore.event.dto;


import lombok.*;
import ru.practicum.explore.enums.StateActionAdmin;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest extends UpdateEventDto {
    private StateActionAdmin stateAction;

}
