package com.example.springweb;

import jakarta.annotation.PostConstruct;
import org.openapitools.client.api.DefaultApi;
import org.openapitools.client.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StartupBean {

    private final DefaultApi defaultApi;

    public StartupBean() {
        this.defaultApi = new DefaultApi();
    }

    @PostConstruct
    public void init() {
        GameInputDto gameInput = new GameInputDto();
        gameInput.setGroupName("Alena, Maurice & Steven");

        GameDto game = defaultApi.gamePost(gameInput);
        System.out.println("======================================");
        System.out.println("🎮 Spiel gestartet für Gruppe: " + game.getGroupName());
        System.out.println("📍 Startposition: " + posToString(game.getPosition()));
        System.out.println("======================================");

        List<String> visited = new ArrayList<>();

        int step = 0;

        while (game.getStatus() == GameStatusDto.ONGOING) {
            int currentX = game.getPosition().getPositionX().intValue();
            int currentY = game.getPosition().getPositionY().intValue();
            String currentKey = currentX + "," + currentY;

            if (!visited.contains(currentKey)) {
                visited.add(currentKey);
            }

            DirectionDto[] directions = getPreferredDirections(currentX, currentY);
            boolean moved = false;

            for (DirectionDto dir : directions) {
                int nextX = currentX;
                int nextY = currentY;

                switch (dir) {
                    case UP -> nextY++;
                    case DOWN -> nextY--;
                    case LEFT -> nextX--;
                    case RIGHT -> nextX++;
                }

                String nextKey = nextX + "," + nextY;
                if (nextX < 1 || nextX > 5 || nextY < 1 || nextY > 5 || visited.contains(nextKey)) {
                    continue;
                }

                MoveInputDto moveInput = new MoveInputDto();
                moveInput.setDirection(dir);

                try {
                    MoveDto move = defaultApi.gameGameIdMovePost(game.getGameId(), moveInput);
                    System.out.println("➡️  Versuche Richtung: " + dir + " → Status: " + move.getMoveStatus());

                    if (move.getMoveStatus() == MoveStatusDto.MOVED) {
                        game.setPosition(move.getPositionAfterMove());
                        game = defaultApi.gameGameIdGet(game.getGameId());

                        step++;
                        System.out.println("--------------------------------------");
                        System.out.println("✅ Schritt: " + step);
                        System.out.println("📍 Neue Position: " + posToString(game.getPosition()));
                        System.out.println("📌 Spielstatus: " + game.getStatus());
                        System.out.println("--------------------------------------");

                        moved = true;
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Fehler bei Richtung " + dir + ": " + e.getMessage());
                }
            }

            if (!moved) {
                System.out.println("🚫 Keine gültige Bewegung mehr möglich. Spiel festgefahren.");
                break;
            }
        }

        System.out.println("======================================");
        System.out.println("🏁 Spiel beendet!");
        System.out.println("📌 Endgültiger Spielstatus: " + game.getStatus());
        System.out.println("📍 Endposition: " + posToString(game.getPosition()));
        System.out.println("======================================");
    }

    private DirectionDto[] getPreferredDirections(int currentX, int currentY) {
        List<DirectionDto> directions = new ArrayList<>();

        if (currentX < 5) directions.add(DirectionDto.RIGHT);
        if (currentY < 5) directions.add(DirectionDto.UP);
        if (currentX > 1) directions.add(DirectionDto.LEFT);
        if (currentY > 1) directions.add(DirectionDto.DOWN);

        return directions.toArray(new DirectionDto[0]);
    }

    private String posToString(PositionDto pos) {
        return pos.getPositionX() + "," + pos.getPositionY();
    }
}