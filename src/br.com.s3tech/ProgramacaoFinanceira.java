package br.com.s3tech;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays; 
import java.util.List;   

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
        BigDecimal codTipTit = financeiroVO.asBigDecimal("CODTIPTIT"); // Captura o Tipo de Título
        
        boolean ignorarRegra = false;
        
        // 1. Verifica se o Tipo de Título é 37 (Se for, já marca para ignorar a regra)
        if (codTipTit != null && codTipTit.intValue() == 37) {
            ignorarRegra = true;
        }
        
        // 2. Verifica se o título nasceu de uma Nota (NUNOTA > 0)
        // Só faz a busca no banco se a regra ainda não foi ignorada pelo passo anterior
        if (!ignorarRegra && nunota != null && nunota.compareTo(BigDecimal.ZERO) > 0) {
            
            // Busca a TOP lá na TGFCAB
            BigDecimal codTipOper = getCodTipOper(nunota);
            
            if (codTipOper != null) {
                int top = codTipOper.intValue();
                
                // Lista limpa e organizada com todas as suas TOPs de Venda
                List<Integer> topsVendas = Arrays.asList(
                    900, 901, 902, 1000, 1001, 1002, 1003, 1004, 
                    1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012, 1013
                );
                
                // O Java verifica automaticamente se a TOP da nota está dentro da lista acima
                if (topsVendas.contains(top)) {
                    ignorarRegra = true; // Se estiver na lista, marcamos para ignorar o bloqueio
                }
            }
        }
        
        // 3. Só aplica a trava se NÃO for para ignorar a regra (Tipo 37 ou TOP de Venda permitida)
        // Bloqueia origem "E" (Lançamento manual no Financeiro) e "F" (Faturamento de outras TOPs não listadas)
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
    // Método: Busca a TOP (Tipo de Operação) na tabela TGFCAB
    // ====================================================================
    private BigDecimal getCodTipOper(BigDecimal nunota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        BigDecimal codTipOper = null;
        
        try {
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            sql = new NativeSql(jdbc);
            
            sql.appendSql("SELECT CODTIPOPER FROM TGFCAB WHERE NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nunota);
            
            ResultSet rs = sql.executeQuery();
            
            if (rs.next()) {
                codTipOper = rs.getBigDecimal("CODTIPOPER");
            }
            
        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
        
        return codTipOper;
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
    // Métodos obrigatórios da interface que não usaremos neste momento
    // ====================================================================

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {}
    
    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {}
    
    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeCommit(TransactionContext ctx) throws Exception {}

}