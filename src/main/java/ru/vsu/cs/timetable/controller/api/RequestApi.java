package ru.vsu.cs.timetable.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import ru.vsu.cs.timetable.config.swagger.annotation.AccessDeniedResponse;
import ru.vsu.cs.timetable.config.swagger.annotation.IncorrectUsernameResponse;
import ru.vsu.cs.timetable.exception.message.ErrorMessage;
import ru.vsu.cs.timetable.model.dto.univ_requests.*;

import java.util.List;

@AccessDeniedResponse
@IncorrectUsernameResponse
@SecurityRequirement(name = "bearer-key")
@Tag(name = "Request API", description = "API для работы с заявками преподавателей")
public interface RequestApi {

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Успешное отправление заявки"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Не пройдена валидация"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = """
                            Id переданной группы не было найдено, \t
                            Переданного инвентаря не существует в базе, \t
                            Неверный username пользователя
                            """,
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(
            summary = "Отправляет заявку для расписания"
    )
    ResponseEntity<Void> sendRequest(
            @Parameter(description = "Вся информация о заявке преподавателя")
            SendRequest sendRequest,
            @Parameter(hidden = true)
            Authentication authentication
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный возврат информации",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ShowSendRequest.class)
                            )
                    }
            )
    })
    @Operation(
            summary = "Возвращает информациию для заявки на составление"
    )
    ResponseEntity<ShowSendRequest> sendRequestInfo(
            @Parameter(hidden = true)
            Authentication authentication
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный перенос пары"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Аудитория занята для переноса, \t
                            Не пройдена валидация
                            """,
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(
            summary = "Отправляет заявку на перенос занятия"
    )
    ResponseEntity<Void> moveClass(
            @Parameter(description = "Вся информация о заявке на перенос")
            MoveClassRequest moveClassRequest,
            @Parameter(hidden = true)
            Authentication authentication
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная отправка информации",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MoveClassResponse.class)
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Расписание ещё не было составлено",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorMessage.class)
                            )
                    }
            )
    })
    @Operation(
            summary = "Отправляет информацию для переноса занятия"
    )
    ResponseEntity<MoveClassResponse> moveClassInfo(
            @Parameter(hidden = true)
            Authentication authentication
    );

    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение заявок",
                    content = {
                            @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = RequestDto.class))
                            )
                    }
            )
    })
    @Operation(
            summary = "Возвращает список заявок"
    )
    ResponseEntity<List<RequestDto>> getFacultyRequests(
            @Parameter(hidden = true)
            Authentication authentication
    );
}
