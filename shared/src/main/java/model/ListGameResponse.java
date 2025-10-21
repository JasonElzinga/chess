package model;

import java.util.Collection;

public record ListGameResponse(Collection<IndividualGameData> games) {
}
