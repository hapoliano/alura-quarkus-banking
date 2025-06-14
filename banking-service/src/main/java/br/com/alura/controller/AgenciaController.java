package br.com.alura.controller;

import br.com.alura.domain.Agencia;
import br.com.alura.service.http.AgenciaService;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/agencias")
public class AgenciaController {

    private final AgenciaService agenciaService;

    AgenciaController(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @POST
    @NonBlocking
    @Transactional
    public Uni<RestResponse<Void>> cadastrar(Agencia agencia, @Context UriInfo uriInfo) {
        return this.agenciaService.cadastrar(agencia).replaceWith(RestResponse.created(uriInfo.getAbsolutePathBuilder().build()));
    }

    @GET
    @RateLimit(value = 5, window = 10)
    @Path("{id}")
    public Uni<RestResponse<Agencia>> buscarPorId(Long id) {
        return this.agenciaService.buscarPorId(id).onItem().transform(RestResponse::ok);
    }

    @DELETE
    @Transactional
    @Path("{id}")
    public Uni<RestResponse<Void>> deletar(Long id) {
        return this.agenciaService.deletar(id).replaceWith(RestResponse.ok());
    }

    @PUT
    @Transactional
    public Uni<RestResponse<Void>> alterar(Agencia agencia) {
        return agenciaService.alterar(agencia).replaceWith(RestResponse.ok());
    }
}
