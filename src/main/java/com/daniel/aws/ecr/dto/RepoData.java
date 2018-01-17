package com.daniel.aws.ecr.dto;

public class RepoData {

	private String name;
	private Long totalSizeImages;

	public RepoData(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getTotalSizeImages() {
		return totalSizeImages;
	}
	public void setTotalSizeImages(Long totalSizeImages) {
		this.totalSizeImages = totalSizeImages;
	}
}
