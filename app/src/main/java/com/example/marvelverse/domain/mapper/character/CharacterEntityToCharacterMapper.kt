package com.example.marvelverse.domain.mapper.character

import com.example.marvelverse.data.dataSources.local.entities.CharacterEntity
import com.example.marvelverse.domain.entities.Character
import com.example.marvelverse.domain.mapper.Mapper
import javax.inject.Inject

class CharacterEntityToCharacterMapper @Inject constructor() : Mapper<CharacterEntity, Character> {
    override fun map(input: CharacterEntity): Character {
        return Character(
            id = input.id,
            name = input.name,
            description = input.description,
            seriesUri = input.series,
            comicsUri = input.comics,
            storiesUri = input.stories,
            eventsUri = input.events,
            imageUrl = input.thumbnail
        )
    }
}