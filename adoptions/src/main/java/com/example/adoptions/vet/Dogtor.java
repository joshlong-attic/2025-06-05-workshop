package com.example.adoptions.vet;

import com.example.adoptions.adoptions.DogAdoptionEvent;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class Dogtor {

    @ApplicationModuleListener
    void checkup(DogAdoptionEvent dogId) throws Exception {
        Thread.sleep(5000);
        System.out.println("Checking up dog " + dogId + "");
    }

}
