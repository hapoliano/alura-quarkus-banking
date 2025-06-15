package br.com.alura.service.messaging;

import br.com.alura.domain.messaging.AgenciaMessage;
import br.com.alura.repository.AgenciaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class RemoverAgenciaConsumer {

    final private ObjectMapper objectMapper;
    final private AgenciaRepository agenciaRepository;

    public RemoverAgenciaConsumer(ObjectMapper objectMapper, AgenciaRepository agenciaRepository, ObjectMapper objectMapper1, AgenciaRepository agenciaRepository1) {
        this.objectMapper = objectMapper1;
        this.agenciaRepository = agenciaRepository1;
    }

    @WithTransaction
    @Incoming("banking-service-channel")
    public Uni<Void> consumirMensagem(String mensagem) {
        return Uni.createFrom().item(() -> {
                    try {
                        Log.info(mensagem);
                        return objectMapper.readValue(mensagem, AgenciaMessage.class);

                    } catch (JsonProcessingException ex) {
                        Log.error(ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                }).onItem()
                .transformToUni(agenciaMessage ->
                        agenciaRepository.findByCnpj(agenciaMessage.getCnpj())
                                .onItem().ifNotNull().transformToUni(agencia -> agenciaRepository.deleteById(agencia.getId()))
                ).replaceWithVoid();
    }
}
