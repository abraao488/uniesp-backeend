package br.com.encomendas.model.enums;

public enum StatusPedido {
    RECEBIDO, AGUARDANDO_MATERIAL, EM_PRODUCAO, PRONTO, ENTREGUE, CANCELADO;

    public boolean podeIrPara(StatusPedido destino) {
        if (destino == null) return false;
        if (this == destino) return false;
        switch (this) {
            case RECEBIDO:
                return destino == AGUARDANDO_MATERIAL || destino == EM_PRODUCAO || destino == CANCELADO;
            case AGUARDANDO_MATERIAL:
                return destino == EM_PRODUCAO || destino == CANCELADO;
            case EM_PRODUCAO:
                return destino == AGUARDANDO_MATERIAL || destino == PRONTO || destino == CANCELADO;
            case PRONTO:
                return destino == ENTREGUE || destino == CANCELADO;
            default:
                return false;
        }
    }
}
