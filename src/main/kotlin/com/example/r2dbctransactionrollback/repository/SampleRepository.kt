package com.example.r2dbctransactionrollback.repository

import com.example.r2dbctransactionrollback.repository.entity.Sample
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SampleRepository : CoroutineCrudRepository<Sample, Long> {
}