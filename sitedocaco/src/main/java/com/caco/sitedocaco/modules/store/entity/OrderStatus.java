package com.caco.sitedocaco.modules.store.entity;

public enum OrderStatus {
    PENDING_PAYMENT,  // Aguardando pagamento (não contabilizado)
    PENDING,          // Pago, aguardando retirada
    DELIVERED         // Entregue/retirado
}
