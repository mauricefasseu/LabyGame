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
        gameInput.setGroupName("mauricefasseu");

        GameDto game = defaultApi.gamePost(gameInput);
        System.out.println("Spiel von " + game.getGroupName() + " gestartet!");
        System.out.println("Startposition: " + game.getPosition());

        List<String> visited = new ArrayList<>();

        while (game.getStatus() == GameStatusDto.ONGOING) {
            int currentX = game.getPosition().getPositionX().intValue();
            int currentY = game.getPosition().getPositionY().intValue();
            String currentKey = currentX + "," + currentY;
            //Füge die aktuelle Position nur hinzu, wenn sie noch nicht besucht wurde
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
                //Prüfe, ob das nächste Feld gültig und noch nicht besucht ist
                if (nextX < 1 || nextX > 5 || nextY < 1 || nextY > 5 || visited.contains(nextKey)) {
                    continue;
                }

                MoveInputDto moveInput = new MoveInputDto();
                moveInput.setDirection(dir);

                try {
                    MoveDto move = defaultApi.gameGameIdMovePost(game.getGameId(), moveInput);
                    System.out.println("Versuche Richtung: " + dir + " → Status: " + move.getMoveStatus());

                    if (move.getMoveStatus() == MoveStatusDto.MOVED) {
                        game.setPosition(move.getPositionAfterMove()); //Aktualisiere die Position
                        moved = true;
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Fehler bei Richtung " + dir + ": " + e.getMessage());
                }
            }

            if (!moved) {
                System.out.println("Keine gültige Bewegung mehr möglich. Spiel festgefahren.");
                break;
            }

            game = defaultApi.gameGameIdGet(game.getGameId());
        }

        System.out.println("Spiel beendet!");
        System.out.println("Endgültiger Spielstatus: " + game.getStatus());
        System.out.println("Endposition: " + game.getPosition());
    }

    private DirectionDto[] getPreferredDirections(int currentX, int currentY) {
        List<DirectionDto> directions = new ArrayList<>();

        if (currentX < 5) directions.add(DirectionDto.RIGHT);

        if (currentY < 5) directions.add(DirectionDto.UP);

        if (currentX > 1) directions.add(DirectionDto.LEFT);

        if (currentY > 1) directions.add(DirectionDto.DOWN);

        return directions.toArray(new DirectionDto[0]);
    }
}