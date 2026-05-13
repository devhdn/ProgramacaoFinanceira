package br.com.s3tech.helper;

import java.math.BigDecimal;
import java.util.Collection;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class NotificacaoHelper {

    /**
     * Envia um aviso de sistema buscando os destinatários na tabela de Limites (Evento 1002).
     * * @param nunota Número da nota que gerou o bloqueio
     * @param codigoEvento Código do evento de liberação (Ex: 1002)
     */
    public static void enviarAvisoBloqueioVendaPorEvento(BigDecimal nunota, Integer codigoEvento) {
        try {
            // 1. Busca quem tem permissão para esse evento na entidade LimiteLiberacao
            JapeWrapper limiteDAO = JapeFactory.dao("LimiteLiberacao");
            Collection<DynamicVO> limites = limiteDAO.find("EVENTO = ?", new Object[]{codigoEvento});

            // Se não houver ninguém configurado, sai do método
            if (limites == null || limites.isEmpty()) {
                System.out.println("Nenhum destinatário configurado para o evento: " + codigoEvento);
                return;
            }

            EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
            String numNotaStr = nunota != null ? nunota.toString() : "Nova Nota";

            // 2. Itera sobre todos os liberadores configurados e envia a notificação
            for (DynamicVO limite : limites) {
                BigDecimal codUsu = limite.asBigDecimal("CODUSU");
                BigDecimal codGru = limite.asBigDecimal("CODGRU");

                DynamicVO avisoVO = (DynamicVO) dwfEntityFacade.getDefaultValueObjectInstance("AvisoSistema");
                avisoVO.setProperty("IDENTIFICADOR", "PERSONALIZADO");
                avisoVO.setProperty("IMPORTANCIA", new BigDecimal(3)); // Alta
                avisoVO.setProperty("TITULO", "Tentativa de Venda à Vista Bloqueada!");
                avisoVO.setProperty("DESCRICAO", "A nota " + numNotaStr + " tentou utilizar a condição de pagamento à vista (Cód 11).");
                avisoVO.setProperty("SOLUCAO", "Verifique a necessidade de liberação manual na central.");
                avisoVO.setProperty("TIPO", "P"); 

                // Lógica nativa do Sankhya: Se CODGRU for maior que zero, é um grupo. Senão, é usuário.
                if (codGru != null && codGru.compareTo(BigDecimal.ZERO) > 0) {
                    avisoVO.setProperty("CODGRU", codGru);
                } else if (codUsu != null && codUsu.compareTo(BigDecimal.ZERO) > 0) {
                    avisoVO.setProperty("CODUSU", codUsu);
                } else {
                    continue; // Ignora se ambos forem nulos ou zero (inconsistência de banco)
                }

                // Cria o aviso individual para este destinatário
                dwfEntityFacade.createEntity("AvisoSistema", (EntityVO) avisoVO);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao tentar gerar Aviso de Sistema via NotificacaoHelper: " + e.getMessage());
            e.printStackTrace();
        }
    }
}