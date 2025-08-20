package com.projectone.PalliativeCare.repository;

import com.projectone.PalliativeCare.model.Activity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityRepository extends MongoRepository<Activity, String> {

}
