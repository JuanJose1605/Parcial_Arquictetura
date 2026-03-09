package com.iglesia;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChurchService {

    private final ChurchRepository churchRepository;

    public ChurchService(ChurchRepository churchRepository) {
        this.churchRepository = churchRepository;
    }

    public ChurchController.ChurchResponse create(ChurchController.ChurchRequest request) {
        if (churchRepository.count() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe una iglesia registrada");
        }

        Church church = new Church();
        church.setName(request.name());
        church.setAddress(request.address());

        churchRepository.save(church);

        return ChurchController.ChurchResponse.from(church);
    }

    public ChurchController.ChurchResponse get() {
        return churchRepository.findAll()
                .stream()
                .findFirst()
                .map(ChurchController.ChurchResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay iglesia registrada"));
    }
}