package com.example.r2dbctransactionrollback.controller

import com.example.r2dbctransactionrollback.repository.entity.Sample
import com.example.r2dbctransactionrollback.service.SampleService
import org.springframework.web.bind.annotation.*

@RestController
class SampleController(
    private val sampleService: SampleService
) {

    @PostMapping("/samples")
    suspend fun createSample(@RequestParam title: String, @RequestParam rollback: Boolean): Sample {
        return sampleService.create(title, rollback)
    }

    @PutMapping("/samples/{id}")
    suspend fun updateSample(@PathVariable id: Long, @RequestParam title: String, @RequestParam rollback: Boolean): Sample {
        return sampleService.update(id, title, rollback)
    }

    @DeleteMapping("/samples/{id}")
    suspend fun deleteSample(@PathVariable id: Long, @RequestParam rollback: Boolean) {
        sampleService.delete(id, rollback)
    }

    @DeleteMapping("/samples")
    suspend fun deleteAllSamples(@RequestParam rollback: Boolean) {
        sampleService.deleteAll(rollback)
    }

    @GetMapping("/samples/{id}")
    suspend fun readSample(@PathVariable id: Long): Sample {
        return sampleService.read(id)
    }

    @GetMapping("/samples")
    suspend fun readAllSamples(): List<Sample> {
        return sampleService.readAll()
    }
}