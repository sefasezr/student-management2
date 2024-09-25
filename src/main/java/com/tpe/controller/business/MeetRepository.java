package com.tpe.controller.business;

import com.tpe.service.business.MeetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController //restApi için ve mapleme için bunu yazarız component yerine
@RequestMapping("/meet")
@RequiredArgsConstructor
public class MeetRepository {

    private final MeetService meetService;
}
