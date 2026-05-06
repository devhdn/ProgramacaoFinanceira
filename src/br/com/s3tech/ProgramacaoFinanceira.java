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
        
        // Ignora a regra se for um tipo de título específico (ex: 37)
        if (codTipTit != null && codTipTit.intValue() == 37) {
            ignorarRegra = true;
        }
        
        // Valida as regras de negócio baseadas na Nota (TOP e Tipo de Negociação)
        if (!ignorarRegra && nunota != null && nunota.compareTo(BigDecimal.ZERO) > 0) {
            if (isOperacaoPermitida(nunota)) {
                ignorarRegra = true;
            }
        }
        
        // Aplica o bloqueio de período financeiro se a regra não foi ignorada
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
    // Método: Verifica dinamicamente se a TOP e o CODTIPVENDA são permitidos
    // ====================================================================
    private boolean isOperacaoPermitida(BigDecimal nunota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        boolean permitido = false;
        
        // ⚠️ INSIRA AQUI A SUA LISTA DE TOPs SEPARADA POR VÍRGULA
        String listaTopsPermitidas = "900,901,902,1000,1001,1002,1003,1004,1005,1006,1007,1008,1009,1010,1011,1012,1013";
        
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            sql = new NativeSql(jdbc);
            
            // A query agora verifica o CODTIPOOPER (TOP) e o CODTIPVENDA
            sql.appendSql("SELECT 1 FROM TGFCAB CAB " +
                          "WHERE CAB.NUNOTA = :NUNOTA " +
                          "AND CAB.CODTIPOOPER IN (" + listaTopsPermitidas + ") " +
                          "AND CAB.CODTIPVENDA IN (" +
                          "    SELECT TPV.CODTIPVENDA FROM TGFTPV TPV " +
                          "    WHERE TPV.SUBTIPOVENDA = 1 AND TPV.ATIVO = 'S'" +
                          ")");
                          
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
            
            sql.appendSql("SELECT PERABT FROM AD_PROFIN WHERE TRUNC(DTINI) <= TRUNC(:DTVENC) AND TRUNC(DTFIN) >= TRUNC(:DTVENC)");
            sql.setNamedParameter("DTVENC", dtVenc);
            
            ResultSet rs = sql.executeQuery();
            
            if (rs.next()) {
                String status = rs.getString("PERABT");
                
                if (status != null) {
                    status = status.trim(); 
                    if ("N".equalsIgnoreCase(status) || "F".equalsIgnoreCase(status) || "NAO".equalsIgnoreCase(status)) {
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

    @Override public void afterInsert(PersistenceEvent event) throws Exception {}
    @Override public void beforeUpdate(PersistenceEvent event) throws Exception {}
    @Override public void afterUpdate(PersistenceEvent event) throws Exception {}
    @Override public void beforeDelete(PersistenceEvent event) throws Exception {}
    @Override public void afterDelete(PersistenceEvent event) throws Exception {}
    @Override public void beforeCommit(TransactionContext ctx) throws Exception {}

}