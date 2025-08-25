package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningMaterialDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningMaterial;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSection;
import com.sfr.tokyo.sfr_backend.exception.EntityNotFoundException;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningMaterialMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningMaterialRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningMaterialService {

    private final LearningMaterialRepository learningMaterialRepository;
    private final LearningSectionRepository learningSectionRepository;
    private final LearningMaterialMapper learningMaterialMapper;

    /**
     * マテリアルを作成
     * 
     * @param materialDto マテリアルDTO
     * @return 作成されたマテリアルDTO
     */
    public LearningMaterialDto createMaterial(LearningMaterialDto materialDto) {
        // セクションの存在確認
        LearningSection section = learningSectionRepository.findById(materialDto.getSectionId())
                .orElseThrow(() -> new EntityNotFoundException("指定されたセクションが見つかりません: " + materialDto.getSectionId()));

        // マテリアル作成
        LearningMaterial material = LearningMaterial.builder()
                .sectionId(materialDto.getSectionId())
                .text(materialDto.getText())
                .mediaUrl(materialDto.getMediaUrl())
                .displayOrder(materialDto.getDisplayOrder())
                .build();

        LearningMaterial savedMaterial = learningMaterialRepository.save(material);
        return learningMaterialMapper.toDto(savedMaterial);
    }

    /**
     * セクションのマテリアル一覧を取得
     * 
     * @param sectionId セクションID
     * @return マテリアルリスト
     */
    @Transactional(readOnly = true)
    public List<LearningMaterialDto> getMaterialsBySectionId(Long sectionId) {
        // セクションの存在確認
        if (!learningSectionRepository.existsById(sectionId)) {
            throw new EntityNotFoundException("指定されたセクションが見つかりません: " + sectionId);
        }

        List<LearningMaterial> materials = learningMaterialRepository.findBySectionIdOrderByDisplayOrderAsc(sectionId);
        return learningMaterialMapper.toDtoList(materials);
    }

    /**
     * マテリアル詳細を取得
     * 
     * @param materialId マテリアルID
     * @return マテリアルDTO
     */
    @Transactional(readOnly = true)
    public LearningMaterialDto getMaterialById(Long materialId) {
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたマテリアルが見つかりません: " + materialId));

        return learningMaterialMapper.toDto(material);
    }

    /**
     * マテリアルを更新
     * 
     * @param materialId  マテリアルID
     * @param materialDto 更新DTO
     * @return 更新されたマテリアルDTO
     */
    public LearningMaterialDto updateMaterial(Long materialId, LearningMaterialDto materialDto) {
        LearningMaterial existingMaterial = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたマテリアルが見つかりません: " + materialId));

        // 更新可能フィールドのみ更新
        existingMaterial.setText(materialDto.getText());
        existingMaterial.setMediaUrl(materialDto.getMediaUrl());
        existingMaterial.setDisplayOrder(materialDto.getDisplayOrder());

        LearningMaterial updatedMaterial = learningMaterialRepository.save(existingMaterial);
        return learningMaterialMapper.toDto(updatedMaterial);
    }

    /**
     * マテリアルを削除
     * 
     * @param materialId マテリアルID
     */
    public void deleteMaterial(Long materialId) {
        if (!learningMaterialRepository.existsById(materialId)) {
            throw new EntityNotFoundException("指定されたマテリアルが見つかりません: " + materialId);
        }

        learningMaterialRepository.deleteById(materialId);
    }
}
