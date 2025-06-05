package com.example.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@SpringBootApplication
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }

    @Bean
    ItemReader<Dog> reader(@Value("file://${HOME}/Desktop/talk/dogs.csv") Resource resource) {
        return new FlatFileItemReaderBuilder<Dog>()
                .resource(resource)
                .name("dogReader")
                .linesToSkip(1)
                .delimited().names("id,name,description,dob,owner,gender,image".split(","))
                .fieldSetMapper(fieldSet ->
                        new Dog(fieldSet.readString("name"), fieldSet.readInt("id"),
                            fieldSet.readString("description")))
                .build();

    }

    @Bean
    ItemWriter<Dog> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder <Dog> ()
                .dataSource(dataSource)
                .sql("INSERT INTO dog (id, name, description) VALUES (?,?,?)")
                .itemPreparedStatementSetter((item, ps) -> {
                    ps.setInt(1, item.id());
                    ps.setString(2, item.name());
                    ps.setString(3, item.description());
                })
                .build() ;
    }

    @Bean
    Step step(JobRepository repository, PlatformTransactionManager tx, ItemReader<Dog> reader,
              ItemWriter<Dog> writer) {
        return new StepBuilder("step", repository)
                .<Dog, Dog>chunk(10, tx)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    Job job(JobRepository repository,
            Step step) {
        return new JobBuilder("job", repository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

}

record Dog(String name, int id, String description) {
}
//id,name,description,dob,owner,gender,image