package br.insper.aposta.aposta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class ApostaService {

    @Autowired
    private ApostaRepository apostaRepository;

    public void salvar(Aposta aposta) {
        aposta.setId(UUID.randomUUID().toString());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RetornarPartidaDTO> partida = restTemplate.getForEntity(
                "http://localhost:8080/partida/" + aposta.getIdPartida(),
                RetornarPartidaDTO.class);

        if (partida.getStatusCode().is2xxSuccessful()) {
            apostaRepository.save(aposta);
        }

    }

    public List<Aposta> listar(String status) {
        if(status != null) return apostaRepository.findByStatus(status);
        return apostaRepository.findAll();
    }

    public Aposta verificaAposta(String id){
        if (!apostaRepository.existsById(id)){
            throw new RuntimeException("erro");
        }

        Aposta aposta = apostaRepository.findById(id).get();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<RetornarPartidaDTO> partida = restTemplate.getForEntity(
                "http://localhost:8080/partida/" + aposta.getIdPartida(),
                RetornarPartidaDTO.class);

        if (partida.getStatusCode().is2xxSuccessful()) {
            if (partida.getBody().getStatus().equals("REALIZADA")){
                if (partida.getBody().getPlacarMandante() > partida.getBody().getPlacarVisitante() && aposta.getResultado().equals("vitoria_mandante")) aposta.setStatus("ganhou");
                else{
                    if (partida.getBody().getPlacarMandante() < partida.getBody().getPlacarVisitante() && aposta.getResultado().equals("vitoria_visitante")) aposta.setStatus("ganhou");
                    else if (partida.getBody().getPlacarMandante() == partida.getBody().getPlacarVisitante() && aposta.getResultado().equals("empate")) aposta.setStatus("ganhou");
                    else aposta.setStatus("perdeu");
                }
            }
        }

        return aposta;

    }

}

