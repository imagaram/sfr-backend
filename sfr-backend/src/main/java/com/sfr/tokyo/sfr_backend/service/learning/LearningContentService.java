package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateDto;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentCreateResponse;
import com.sfr.tokyo.sfr_backend.dto.learning.LearningContentDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSpace;
import com.sfr.tokyo.sfr_backend.exception.EntityNotFoundException;
import com.sfr.tokyo.sfr_backend.exception.InvalidRequestException;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningContentMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningContentRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningContentService {

    private final LearningContentRepository learningContentRepository;
    private final LearningSpaceRepository learningSpaceRepository;
    private final LearningContentMapper learningContentMapper;

    /**
     * 学習コンテンツを作成
     * 
     * @param createDto 作成DTO
     * @return 作成レスポンス
     */
    public LearningContentCreateResponse createContent(LearningContentCreateDto createDto) {
        // 学習空間の存在確認
        LearningSpace space = learningSpaceRepository.findById(createDto.getSpaceId())
                .orElseThrow(() -> new EntityNotFoundException("指定された学習空間が見つかりません: " + createDto.getSpaceId()));

        // DTOからEntityに変換
        LearningContent content = learningContentMapper.toEntity(createDto);

        // 保存
        LearningContent savedContent = learningContentRepository.save(content);

        return LearningContentCreateResponse.builder()
                .id(savedContent.getId())
                .message("学習コンテンツが正常に作成されました")
                .build();
    }

    /**
     * 学習空間のコンテンツ一覧を取得
     * 
     * @param spaceId 学習空間ID
     * @return コンテンツリスト
     */
    @Transactional(readOnly = true)
    public List<LearningContentDto> getContentsBySpaceId(Long spaceId) {
        // 学習空間の存在確認
        if (!learningSpaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("指定された学習空間が見つかりません: " + spaceId);
        }

        List<LearningContent> contents = learningContentRepository.findBySpaceIdOrderByIdAsc(spaceId);
        return learningContentMapper.toDtoList(contents);
    }

    /**
     * コンテンツタイプ別のコンテンツ一覧を取得
     * 
     * @param spaceId 学習空間ID
     * @param type    コンテンツタイプ
     * @return コンテンツリスト
     */
    @Transactional(readOnly = true)
    public List<LearningContentDto> getContentsBySpaceIdAndType(Long spaceId, LearningContent.ContentType type) {
        // 学習空間の存在確認
        if (!learningSpaceRepository.existsById(spaceId)) {
            throw new EntityNotFoundException("指定された学習空間が見つかりません: " + spaceId);
        }

        List<LearningContent> contents = learningContentRepository.findBySpaceIdAndTypeOrderByIdAsc(spaceId, type);
        return learningContentMapper.toDtoList(contents);
    }

    /**
     * コンテンツ詳細を取得
     * 
     * @param contentId コンテンツID
     * @return コンテンツDTO
     */
    @Transactional(readOnly = true)
    public LearningContentDto getContentById(Long contentId) {
        LearningContent content = learningContentRepository.findByIdWithSectionsAndMaterials(contentId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたコンテンツが見つかりません: " + contentId));

        return learningContentMapper.toDto(content);
    }

    /**
     * コンテンツを更新
     * 
     * @param contentId コンテンツID
     * @param updateDto 更新DTO
     * @return 更新されたコンテンツDTO
     */
    public LearningContentDto updateContent(Long contentId, LearningContentCreateDto updateDto) {
        LearningContent existingContent = learningContentRepository.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたコンテンツが見つかりません: " + contentId));

        // 学習空間IDの変更は許可しない
        if (!existingContent.getSpaceId().equals(updateDto.getSpaceId())) {
            throw new InvalidRequestException("学習空間IDは変更できません");
        }

        // 更新可能フィールドのみ更新
        existingContent.setTitle(updateDto.getTitle());
        existingContent.setType(updateDto.getType());
        existingContent.setUrl(updateDto.getUrl());
        existingContent.setDescription(updateDto.getDescription());

        LearningContent updatedContent = learningContentRepository.save(existingContent);
        return learningContentMapper.toDto(updatedContent);
    }

    /**
     * コンテンツを削除
     * 
     * @param contentId コンテンツID
     */
    public void deleteContent(Long contentId) {
        if (!learningContentRepository.existsById(contentId)) {
            throw new EntityNotFoundException("指定されたコンテンツが見つかりません: " + contentId);
        }

        learningContentRepository.deleteById(contentId);
    }
}
