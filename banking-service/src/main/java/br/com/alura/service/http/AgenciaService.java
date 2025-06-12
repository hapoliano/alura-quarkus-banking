package br.com.alura.service.http;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.http.AgenciaHttp;
import br.com.alura.domain.http.SituacaoCadastral;
import br.com.alura.exceptions.AgenciaNaoAtivaOuNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import br.com.alura.service.SituacaoCadastralHttpService;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AgenciaService {

    private final AgenciaRepository agenciaRepository;
    private final MeterRegistry meterRegistry;

    AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
    }

    @RestClient
    SituacaoCadastralHttpService situacaoCadastralHttpService;

    @WithTransaction
    public Uni<Void> cadastrar(Agencia agencia) {
        Uni<AgenciaHttp> agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        return agenciaHttp
                .onItem().ifNull().failWith(new AgenciaNaoAtivaOuNaoEncontradaException())
                .onItem().transformToUni(item -> persistirSeAtiva(agencia, item));

    }

    private Uni<Void> persistirSeAtiva(Agencia agencia, AgenciaHttp agenciaHttp) {
        if (agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
            Log.info("A agência com o CNPJ " + agencia.getCnpj() + " foi cadastrada");
            meterRegistry.counter("agencia_adcionada_counter").increment();
            return agenciaRepository.persist(agencia).replaceWithVoid();
        } else {
            Log.info("A agência com o CNPJ " + agencia.getCnpj() + " não foi cadastrada");
            meterRegistry.counter("agencia_nao_adcionada _counter").increment();
            return Uni.createFrom().failure(new AgenciaNaoAtivaOuNaoEncontradaException());
        }
    }

    @WithSession
    public Uni<Agencia> buscarPorId(Long id) {
        return agenciaRepository.findById(id);
    }

    @WithTransaction
    public Uni<Void> deletar(Long id) {
        Log.info("A agência com o id " + id + " foi deletada");
        return agenciaRepository.deleteById(id).replaceWithVoid();
    }

    @WithTransaction
    public Uni<Void> alterar(Agencia agencia) {
        Log.info("A agência com o CNPJ " + agencia.getCnpj() + " foi alterada");
        return agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId()).replaceWithVoid();
    }
}
