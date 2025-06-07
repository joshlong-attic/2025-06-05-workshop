package com.example.adoptions.adoptions;

import com.example.adoptions.grpc.AdoptionsGrpc;
import com.example.adoptions.grpc.Dogs;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
class DogAdoptionGrpcService extends AdoptionsGrpc.AdoptionsImplBase {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGrpcService(DogAdoptionService dogAdoptionService) {
        super();
        this.dogAdoptionService = dogAdoptionService;
    }

    @Override
    public void all(Empty request, StreamObserver<Dogs> responseObserver) {
        var dogs = dogAdoptionService
                .dogs()
                .stream()
                .map(dog -> com.example.adoptions.grpc.Dog
                        .newBuilder()
                        .setId(dog.id())
                        .setName(dog.name())
                        .build())
                .toList();
        responseObserver.onNext(Dogs.newBuilder().addAllDogs(dogs).build());
        responseObserver.onCompleted();
    }

}


record Shelter(String location) {
}

@Controller
class DogAdoptionGraphqlController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionGraphqlController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @BatchMapping
    Map<Dog, Shelter> shelter(List<Dog> dogList) {
        // todo make a network call
        // todo select  * from shelter where dog_id IN (?,?,?,..)
        System.out.println("getting shelters for " + dogList);
        var map = new HashMap<Dog, Shelter>();
        for (var d : dogList)
            map.put(d, new Shelter("Utrecht"));
        return map;
    }

    @QueryMapping
    Collection<Dog> dogs() {
        return dogAdoptionService.dogs();
    }
}

@Controller
@ResponseBody
class DogAdoptionHttpController {

    private final DogAdoptionService dogAdoptionService;

    DogAdoptionHttpController(DogAdoptionService dogAdoptionService) {
        this.dogAdoptionService = dogAdoptionService;
    }

    @GetMapping("/dogs")
    Collection<Dog> dogs() {
        return dogAdoptionService.dogs();
    }

    @PostMapping("/dogs/{dogId}/adoptions")
    void adopt(@PathVariable int dogId, @RequestParam String owner) {
        dogAdoptionService.adopt(dogId, owner);
    }
}

@Service
@Transactional
class DogAdoptionService {

    private final DogRepository dogRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    DogAdoptionService(DogRepository dogRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.dogRepository = dogRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    Collection<Dog> dogs() {
        return dogRepository.findAll();
    }

    void adopt(int dogId, String owner) {
        dogRepository.findById(dogId).ifPresent(dog -> {
            var updated = dogRepository
                    .save(new Dog(dog.id(), dog.name(), owner, dog.description()));
            applicationEventPublisher.publishEvent(new DogAdoptionEvent(dogId));
            System.out.println("Updated dog: " + updated);
        });
    }
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

// look mom no lombok!
record Dog(@Id int id, String name, String owner, String description) {
}