package me.abhilashbhyrava.repository;

import me.abhilashbhyrava.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    Task findByTaskName(String taskName);
    void deleteByTaskName(String taskName);
}
