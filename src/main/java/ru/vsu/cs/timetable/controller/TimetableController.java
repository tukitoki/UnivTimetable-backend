package ru.vsu.cs.timetable.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vsu.cs.timetable.controller.api.TimetableApi;
import ru.vsu.cs.timetable.model.dto.TimetableResponse;
import ru.vsu.cs.timetable.logic.service.TimetableService;

@RequiredArgsConstructor
@RequestMapping("/schedule")
@RestController
public class TimetableController implements TimetableApi {

    private final TimetableService timetableService;

    @Override
    @PreAuthorize("hasAnyAuthority('GET_SCHEDULE')")
    @GetMapping
    public ResponseEntity<TimetableResponse> getTimetable(Authentication authentication) {
        String username = authentication.getName();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(timetableService.getTimetable(username));
    }

    @Override
    @SneakyThrows
    @PreAuthorize("hasAnyAuthority('GET_SCHEDULE')")
    @GetMapping("/download")
    public ResponseEntity<Void> downloadTimetable(HttpServletResponse httpServletResponse, Authentication authentication) {
        String username = authentication.getName();
        var workBook = timetableService.downloadTimetable(username);

        httpServletResponse.setContentType("application/vnd.ms-excel");
        httpServletResponse.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=timetable.xlsx");

        workBook.write(httpServletResponse.getOutputStream());
        workBook.close();

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @Override
    @PreAuthorize("hasAuthority('MAKE_TIMETABLE_AUTHORITY')")
    @PostMapping("/make")
    public ResponseEntity<Void> makeTimetable(Authentication authentication) {
        String username = authentication.getName();
        timetableService.makeTimetable(username);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
