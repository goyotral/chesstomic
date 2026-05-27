package com.chesstomic.backend.config;

import org.springframework.stereotype.Component;

@Component
public class EloCalculator {

    private static final int K = 32;

    public int calcularElo(int eloJugador,
                           int eloRival,
                           double resultado) {

        double esperado = 1.0 /
                (1.0 + Math.pow(10,
                (eloRival - eloJugador) / 400.0));

        return (int) Math.round(
                eloJugador + K * (resultado - esperado)
        );
    }
}