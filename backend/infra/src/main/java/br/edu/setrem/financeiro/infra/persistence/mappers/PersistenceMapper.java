package br.edu.setrem.financeiro.infra.persistence.mappers;

import br.edu.setrem.financeiro.domain.entities.Categoria;
import br.edu.setrem.financeiro.domain.entities.Conta;
import br.edu.setrem.financeiro.domain.entities.Transacao;
import br.edu.setrem.financeiro.domain.entities.Usuario;
import br.edu.setrem.financeiro.domain.enums.TipoCategoriaEnum;
import br.edu.setrem.financeiro.domain.enums.TipoTransacaoEnum;
import br.edu.setrem.financeiro.infra.persistence.entities.CategoriaEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.ContaEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.TransacaoEntity;
import br.edu.setrem.financeiro.infra.persistence.entities.UsuarioEntity;

/**
 * Converte entidades de persistência JPA para entidades puras de domínio.
 */
public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static Usuario toDomain(UsuarioEntity e) {
        return new Usuario(e.id, e.nome, e.email, e.senhaHash);
    }

    public static Conta toDomain(ContaEntity e) {
        return new Conta(e.id, e.descricao, e.saldoInicial, e.usuario.id);
    }

    public static Categoria toDomain(CategoriaEntity e) {
        return new Categoria(e.id, e.descricao, TipoCategoriaEnum.fromCodigo(e.tipo), e.usuario.id);
    }

    public static Transacao toDomain(TransacaoEntity e) {
        Transacao t = new Transacao();
        t.setId(e.id);
        t.setDescricao(e.descricao);
        t.setValor(e.valor);
        t.setTipo(TipoTransacaoEnum.fromCodigo(e.tipo));
        t.setDatatransacao(e.datatransacao);
        t.setTransferenciaId(e.transferenciaId);
        t.setContaId(e.conta.id);
        t.setContaDescricao(e.conta.descricao);
        t.setCategoriaId(e.categoria.id);
        t.setCategoriaDescricao(e.categoria.descricao);
        t.setUsuarioId(e.usuario.id);
        return t;
    }
}
