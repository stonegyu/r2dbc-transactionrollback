package com.example.r2dbctransactionrollback.service

import com.example.r2dbctransactionrollback.repository.SampleRepository
import com.example.r2dbctransactionrollback.repository.entity.Sample
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SampleService(
    private val sampleRepository: SampleRepository
) {
    suspend fun create(title: String, rollback: Boolean): Sample {
        val created = sampleRepository.save(Sample(title = title))
        if (rollback) {
            throw RuntimeException("be expected rollback!")
        }
        return created
    }

    suspend fun update(id: Long, title: String, rollback: Boolean): Sample {
        val read = sampleRepository.findById(id) ?: throw RuntimeException("not found")
        read.title = title
        val saved = sampleRepository.save(read)
        if (rollback) {
            throw RuntimeException("be expected rollback!")
        }
        return saved;
    }

    suspend fun delete(id: Long, rollback: Boolean) {
        sampleRepository.deleteById(id)
        if (rollback) {
            throw RuntimeException("be expected rollback!")
        }
    }

    suspend fun deleteAll(rollback: Boolean) {
        sampleRepository.deleteAll()
        if (rollback) {
            throw RuntimeException("be expected rollback!")
        }
    }

    @Transactional(readOnly = true)
    suspend fun read(id: Long): Sample {
        return sampleRepository.findById(id) ?: throw RuntimeException("not found")
    }

    @Transactional(readOnly = true)
    suspend fun readAll(): List<Sample> {
        return sampleRepository.findAll().toList()
    }
}