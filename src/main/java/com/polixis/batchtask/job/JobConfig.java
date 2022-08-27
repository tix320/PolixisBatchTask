package com.polixis.batchtask.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import com.polixis.batchtask.entity.Person;
import com.polixis.batchtask.job.dto.PersonRecord;
import com.polixis.batchtask.util.DateFormatterUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;

@Configuration
@EnableBatchProcessing
public class JobConfig {

	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendOptional(
					DateTimeFormatter.ofPattern("dd/MM/yyyy"))
			.appendOptional(new DateTimeFormatterBuilder().appendPattern("MMMM ")
					.appendText(ChronoField.DAY_OF_MONTH, DateFormatterUtils.getOrdinalSuffixMap())
					.appendPattern(", yyyy")
					.toFormatter())
			.toFormatter();

	@Value("${job.processingChunkSize}")
	private int processingChunkSize;


	@Value("${job.parseErrorSkipLimit}")
	private int parseErrorSkipLimit;

	/**
	 * Formatter for parsing dates like "20/02/2021" or "January 26th, 2021".
	 */

	@Bean
	@StepScope
	public ItemStreamReader<PersonRecord> itemReader() {
		return new ZipItemReader<>(new ClassPathResource("data.zip"),
				inputStream -> new FlatFileItemReaderBuilder<PersonRecord>().name("personsDataReader")
						.resource(new InputStreamResource(inputStream))
						.linesToSkip(1)
						.fieldSetMapper(fieldSet -> new PersonRecord(fieldSet.readString(0), fieldSet.readString(1),
								LocalDate.parse(fieldSet.readString(2), DATE_FORMATTER)))
						.delimited()
						.delimiter(",")
						.names("firstName", "lastName", "date")
						.build());
	}

	@Bean
	public ItemProcessor<PersonRecord, Person> dtoConverter() {
		return item -> new Person(item.firstName(), item.lastName(), item.birthDate());
	}

	@Bean
	public JpaItemWriter<Person> dbWriter(EntityManagerFactory entityManagerFactory) {
		JpaItemWriter<Person> jpaItemWriter = new JpaItemWriter<>();
		jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
		return jpaItemWriter;
	}

	@Bean
	public Step loadPersonDataStep(StepBuilderFactory stepBuilderFactory, ItemStreamReader<PersonRecord> itemReader,
								   JpaItemWriter<Person> dbWriter) {
		return stepBuilderFactory.get("loadPersonsDataStep")
				.<PersonRecord, Person>chunk(processingChunkSize)
				.reader(itemReader)
				.processor(dtoConverter())
				.writer(dbWriter)
				.faultTolerant()
				.skipPolicy(new LimitCheckingItemSkipPolicy(parseErrorSkipLimit, Map.of(FlatFileParseException.class, true)))
				.build();
	}

	@Bean
	public Job importPersonDataJob(JobBuilderFactory jobBuilderFactory, Step loadCsvStep) {
		return jobBuilderFactory.get("importPersonsDataJob")
				.incrementer(new RunIdIncrementer())
				.start(loadCsvStep)
				.build();
	}

}
