package br.com.s3tech;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class ProgramacaoFinanceira implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {

        DynamicVO financeiroVO = (DynamicVO) event.getVo();

        String origem = financeiroVO.asString("ORIGEM");
        BigDecimal nunota = financeiroVO.asBigDecimal("NUNOTA");
        BigDecimal codTipTit = financeiroVO.asBigDecimal("CODTIPTIT");

        boolean ignorarRegra = false;

        // 1. Ignora a regra se for um tipo de título específico (ex: 37)
        if (codTipTit != null && codTipTit.intValue() == 11) {
            ignorarRegra = true;
        }

        // 2. Valida se a TOP deve ser desconsiderada OU se o Tipo de Negociação libera
        // a trava
        if (!ignorarRegra && nunota != null && nunota.compareTo(BigDecimal.ZERO) > 0) {
            if (isOperacaoPermitida(nunota)) {
                ignorarRegra = true;
            }
        }

        // 3. Aplica o bloqueio de período financeiro se a regra não foi ignorada
        if (!ignorarRegra && ("E".equals(origem) || "F".equals(origem))) {

            Timestamp dtVenc = financeiroVO.asTimestamp("DTVENC");

            if (dtVenc != null) {
                boolean periodoFechado = isPeriodoFechado(dtVenc);

                if (periodoFechado) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                    throw new Exception("<br><b>Ação Bloqueada:</b><br>"
                            + "Programação financeira fechada para o dia <b>" + sdf.format(dtVenc) + "</b>.<br>"
                            + "Por favor, entre em contato com o departamento financeiro para analisar.");
                }
            }
        }
    }

    // ====================================================================
    // Método: Verifica se a TOP é exceção OU se o CODTIPVENDA é permitido
    // ====================================================================
    private boolean isOperacaoPermitida(BigDecimal nunota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        boolean permitido = false;

        // 1. Busca o parâmetro usando o método estático da sua NativeSql
        // Ele já gerencia a abertura e fechamento da sessão internamente para esta
        // busca
        String listaTops = NativeSql.getString("TEXTO", "TSIPAR", "CHAVE = 'ISOPERPERM'");

        if (listaTops == null || listaTops.trim().isEmpty()) {
            listaTops = "0";
        }

        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            sql = new NativeSql(jdbc);

            // 2. Monta a consulta principal
            sql.appendSql("SELECT 1 FROM TGFCAB CAB ");
            sql.appendSql("WHERE CAB.NUNOTA = :NUNOTA ");
            sql.appendSql("AND ( ");
            sql.appendSql("    CAB.CODTIPOPER IN (" + listaTops + ") ");
            sql.appendSql("    OR CAB.CODTIPVENDA IN ( ");
            sql.appendSql("        SELECT TPV.CODTIPVENDA FROM TGFTPV TPV ");
            sql.appendSql("        WHERE TPV.SUBTIPOVENDA = 1 AND TPV.ATIVO = 'S' ");
            sql.appendSql("    ) ");
            sql.appendSql(") ");

            sql.setNamedParameter("NUNOTA", nunota);

            ResultSet rs = sql.executeQuery();
            if (rs.next()) {
                permitido = true;
            }

        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }

        return permitido;
    }

    // ====================================================================
    // Método auxiliar para consultar a AD_PROFIN
    // ====================================================================
    private boolean isPeriodoFechado(Timestamp dtVenc) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        boolean fechado = false;

        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            sql = new NativeSql(jdbc);

            sql.appendSql(
                    "SELECT PERABT FROM AD_PROFIN WHERE TRUNC(DTINI) <= TRUNC(:DTVENC) AND TRUNC(DTFIN) >= TRUNC(:DTVENC)");
            sql.setNamedParameter("DTVENC", dtVenc);

            ResultSet rs = sql.executeQuery();

            if (rs.next()) {
                String status = rs.getString("PERABT");

                if (status != null) {
                    status = status.trim();
                    if ("N".equalsIgnoreCase(status) || "F".equalsIgnoreCase(status)
                            || "NAO".equalsIgnoreCase(status)) {
                        fechado = true;
                    }
                }
            }

        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }

        return fechado;
    }

    // ====================================================================
    // Métodos obrigatórios da interface
    // ====================================================================

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {
    }

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {
    }

    @Override
    public void beforeCommit(TransactionContext ctx) throws Exception {
    }
}