package br.com.alura;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InativarAgenciaProducer {

    private KafkaConfiguration kafkaConfiguration;

    public InativarAgenciaProducer(KafkaConfiguration kafkaConfiguration) {
        this.kafkaConfiguration = kafkaConfiguration;
    }

    public void enviarMensagem(Agencia agencia) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String agenciaConvertida = objectMapper.writeValueAsString(agencia);
            kafkaConfiguration.enviarMensagem("remover-agencia", agenciaConvertida);
        } catch (JsonProcessingException ex) {
            Log.error(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
}
