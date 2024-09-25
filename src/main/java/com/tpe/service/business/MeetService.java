package com.tpe.service.business;

import com.tpe.repository.business.MeetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service //şuan sadede okunabilirlik için component yerine kullanılıyor
@RequiredArgsConstructor
public class MeetService {
    private final MeetRepository meetRepository;

}
