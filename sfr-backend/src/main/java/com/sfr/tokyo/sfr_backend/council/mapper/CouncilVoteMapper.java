package com.sfr.tokyo.sfr_backend.council.mapper;

import com.sfr.tokyo.sfr_backend.council.dto.CouncilVoteDto;
import com.sfr.tokyo.sfr_backend.entity.council.CouncilVote;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouncilVoteMapper {
    @Mapping(source = "election.id", target = "electionId")
    @Mapping(source = "candidate.id", target = "candidateId")
    @Mapping(source = "user.id", target = "userId")
    CouncilVoteDto toDto(CouncilVote entity);
    List<CouncilVoteDto> toDtoList(List<CouncilVote> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "election", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    CouncilVote toEntity(CouncilVoteDto dto);
}
