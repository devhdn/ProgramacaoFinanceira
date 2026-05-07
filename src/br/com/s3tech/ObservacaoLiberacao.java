package br.com.s3tech;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;

public class ObservacaoLiberacao implements EventoProgramavelJava {

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {
        processarObservacao(event);
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
        processarObservacao(event);
    }

    // 🔹 Método criado para centralizar a lógica e evitar repetição de código
    private void processarObservacao(PersistenceEvent event) throws Exception {
        DynamicVO tsilibVO = (DynamicVO) event.getVo();

        String tabela = tsilibVO.asString("TABELA");
        java.math.BigDecimal eventoId = tsilibVO.asBigDecimal("EVENTO");

        // Verifica se a liberação é da tabela de financeiro (TGFFIN) e evento 24
        if ("TGFFIN".equals(tabela) && eventoId != null && eventoId.intValue() == 24) {

            java.math.BigDecimal nuFin = tsilibVO.asBigDecimal("NUCHAVE");
            
            // Pega também a sequência para garantir que estamos atualizando a linha exata
            java.math.BigDecimal sequencia = tsilibVO.asBigDecimal("SEQUENCIA");

            // Define o texto automático
            String observacaoAuto = "Observação automática gravada após salvar. Título: " + nuFin;
            String observacaoAtual = tsilibVO.asString("OBSERVACAO");

            if (observacaoAtual == null || !observacaoAtual.equals(observacaoAuto)) {
                
                JapeWrapper tsilibDAO = JapeFactory.dao(event.getEntity().getName());
                
                // 🔹 CORREÇÃO: Usamos o findOne para buscar o registro explicitamente
                // pelas 4 chaves primárias da TSILIB, sem depender de conversão de Object[]
                DynamicVO registroBanco = tsilibDAO.findOne(
                    "TABELA = ? AND EVENTO = ? AND NUCHAVE = ? AND SEQUENCIA = ?",
                    tabela, eventoId, nuFin, sequencia
                );
                
                // Aplica o update na instância recém-buscada
                if (registroBanco != null) {
                    tsilibDAO.prepareToUpdate(registroBanco)
                             .set("OBSERVACAO", observacaoAuto)
                             .update();
                }
            }
        }
    }

    // ===========================================================================
    // Métodos não utilizados (obrigatórios pela interface)
    // ===========================================================================

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {}

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeCommit(TransactionContext ctx) throws Exception {}
}