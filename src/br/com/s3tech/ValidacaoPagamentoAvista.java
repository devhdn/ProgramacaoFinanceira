package br.com.s3tech;

import java.math.BigDecimal;
import java.sql.Timestamp;

import br.com.sankhya.extensions.regrasnegocio.ContextoRegra;
import br.com.sankhya.extensions.regrasnegocio.RegraNegocioJava;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.LiberacaoSolicitada;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;

public class ValidacaoPagamentoAvista implements RegraNegocioJava {

    @Override
    public void executa(ContextoRegra ctx) throws Exception {
        
        BigDecimal nunota = ctx.getNunota();
        
        if (nunota == null) {
            return; 
        }
        
        JapeWrapper cabecalhoNotaDAO = JapeFactory.dao("CabecalhoNota");
        DynamicVO notaVO = cabecalhoNotaDAO.findByPK(nunota);

        if (notaVO != null) {
            
            Integer codCondicaoPagamento = notaVO.asInt("CODTIPVENDA");
            
            // Verifica se a condição de pagamento é 11 (À vista)
            if (codCondicaoPagamento != null && codCondicaoPagamento == 11) {
                
                // 1. Defina o ID do Evento de Liberação (Deve estar cadastrado no Sankhya)
                // Exemplo: 1, 2000, 3500... Substitua pelo ID real do seu evento.
                int idEventoLiberacao = 1; 

                // 2. Cria o objeto de Liberação Solicitada
                LiberacaoSolicitada ls = new LiberacaoSolicitada(
                        nunota, 
                        "TGFCAB", 
                        idEventoLiberacao, 
                        ctx.getUsuarioLogado()
                );
                
                // 3. Preenche as propriedades necessárias da liberação
                BigDecimal vlrNota = notaVO.asBigDecimal("VLRNOTA");
                BigDecimal codCenCus = notaVO.asBigDecimal("CODCENCUS");
                
                ls.setCodCenCus(codCenCus);
                ls.setSolicitante(ctx.getUsuarioLogado());
                ls.setLiberador(BigDecimal.ZERO); // ZERO = Envia para a fila de quem tem alçada configurada
                ls.setVlrAtual(vlrNota != null ? vlrNota : BigDecimal.ZERO);
                ls.setVlrLimite(BigDecimal.ZERO);
                ls.setDescricao("Aprovação exigida: Tentativa de venda com condição de pagamento à vista (Código 11).");
                ls.setDhSolicitacao(new Timestamp(System.currentTimeMillis()));
                
                // 4. Grava a liberação no banco (TSILIB) e processa a alçada
                LiberacaoAlcadaHelper.inserirSolicitacao(ls);
                LiberacaoAlcadaHelper.processarLiberacao(ls);

                // 5. Interrompe o processo e avisa o usuário
                ctx.mostraErro("Operação bloqueada: Vendas à vista exigem liberação. Uma solicitação foi enviada para a gerência!");
                
            } else {
                 ctx.setMensagem("Validação de pagamento concluída com sucesso.");
            }
        }
    }
}