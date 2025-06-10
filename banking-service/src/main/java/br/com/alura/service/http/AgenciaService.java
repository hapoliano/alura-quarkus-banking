package br.com.alura.service.http;

import br.com.alura.domain.Agencia;
import br.com.alura.domain.http.AgenciaHttp;
import br.com.alura.domain.http.SituacaoCadastral;
import br.com.alura.exceptions.AgenciaNaoAtivaOuNaoEncontradaException;
import br.com.alura.repository.AgenciaRepository;
import br.com.alura.service.SituacaoCadastralHttpService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.quarkus.logging.Log;
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

    public void cadastrar(Agencia agencia) {
        Timer timer = this.meterRegistry.timer("cadastrar_agencia_timer");
        timer.record(() -> {
            AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
            if (agenciaHttp != null && agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.ATIVO)) {
                Log.info("A agência com o CNPJ " + agencia.getCnpj() + " foi cadastrada");
                meterRegistry.counter("agencia_adcionada_counter").increment();
                agenciaRepository.persist(agencia);
            } else {
                Log.info("A agência com o CNPJ " + agencia.getCnpj() + " não foi cadastrada");
                meterRegistry.counter("agencia_nao_adcionada _counter").increment();
                throw new AgenciaNaoAtivaOuNaoEncontradaException();
            }
        });
    }

    public Agencia buscarPorId(Long id) {
        return agenciaRepository.findById(id);
    }

    public void deletar(Long id) {
        Log.info("A agência com o id " + id  + " foi deletada");
        agenciaRepository.deleteById(id);
    }

    public void alterar(Agencia agencia) {
        Log.info("A agência com o CNPJ " + agencia.getCnpj() + " foi alterada");
        agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId());
    }
}
