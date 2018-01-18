package com.daniel.aws.ecr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.amazonaws.services.ecr.AmazonECR;
import com.amazonaws.services.ecr.AmazonECRClientBuilder;
import com.amazonaws.services.ecr.model.DescribeImagesRequest;
import com.amazonaws.services.ecr.model.DescribeImagesResult;
import com.amazonaws.services.ecr.model.DescribeRepositoriesRequest;
import com.amazonaws.services.ecr.model.DescribeRepositoriesResult;
import com.amazonaws.services.ecr.model.ImageDetail;
import com.daniel.aws.ecr.dto.RepoData;

public class App {

	public static void main( String[] args ) {
		AmazonECR client = AmazonECRClientBuilder.standard().build();

		//descobre todos os nomes de repositorio do ECR
		Map<RepoData, List<ImageDetail>> repoNameImageDetailMap = new HashMap<>();
		DescribeRepositoriesResult describeRepositoriesRSP = null;
		DescribeRepositoriesRequest describeRepositoriesRQST = new DescribeRepositoriesRequest();
		do {
			describeRepositoriesRSP = client.describeRepositories(describeRepositoriesRQST);
			if(describeRepositoriesRSP != null && describeRepositoriesRSP.getSdkHttpMetadata() != null &&
					describeRepositoriesRSP.getSdkHttpMetadata().getHttpStatusCode() >= 200 && 
							describeRepositoriesRSP.getSdkHttpMetadata().getHttpStatusCode() < 300 &&
									describeRepositoriesRSP.getRepositories() != null && 
									!describeRepositoriesRSP.getRepositories().isEmpty()) {

				describeRepositoriesRSP.getRepositories().forEach(repo -> {
					repoNameImageDetailMap.put(new RepoData(repo.getRepositoryName()), new ArrayList<ImageDetail>());
				});
			}
			if( describeRepositoriesRSP.getNextToken() != null && !describeRepositoriesRSP.getNextToken().isEmpty() ) {
				describeRepositoriesRQST.setNextToken(describeRepositoriesRSP.getNextToken());
			}
		} while (describeRepositoriesRSP != null && describeRepositoriesRSP.getNextToken() != null && !describeRepositoriesRSP.getNextToken().isEmpty());

		//lista os dados de cada imagem de um dado repostorio
		if(!repoNameImageDetailMap.isEmpty()) {
			DescribeImagesRequest describeImagesRQST = null;
			DescribeImagesResult describeImagesRSP = null;
			for(RepoData repoData : repoNameImageDetailMap.keySet()) {
				do {
					describeImagesRQST = new DescribeImagesRequest().withRepositoryName(repoData.getName());
					if(describeImagesRSP != null && describeImagesRSP.getNextToken() != null && !describeImagesRSP.getNextToken().isEmpty() ) {
						describeImagesRQST.setNextToken(describeImagesRSP.getNextToken());
					}
					describeImagesRSP = client.describeImages(describeImagesRQST);
					if(describeImagesRSP != null && describeImagesRSP.getSdkHttpMetadata() != null &&
							describeImagesRSP.getSdkHttpMetadata().getHttpStatusCode() >= 200 && 
									describeImagesRSP.getSdkHttpMetadata().getHttpStatusCode() < 300 &&
									describeImagesRSP.getImageDetails() != null && 
											!describeImagesRSP.getImageDetails().isEmpty()) {

						describeImagesRSP.getImageDetails().forEach(imageDetail-> {
							repoNameImageDetailMap.get(repoData).add(imageDetail);
						});
					}
				} while (describeImagesRSP != null && describeImagesRSP.getNextToken() != null && !describeImagesRSP.getNextToken().isEmpty());
			}
		}

		//calcula o tamanho total em bytes de cada repositorio
		repoNameImageDetailMap.forEach((repoData, list) -> {
			List<ImageDetail> imageDetailList = (List<ImageDetail>) list;
			if(!imageDetailList.isEmpty()) {
				long size = 0;
				for(ImageDetail i : imageDetailList) {
					size += i.getImageSizeInBytes();
				}
				repoData.setTotalSizeImages(size);
				System.out.println("Repositorio " + repoData.getName() + " possui o total de " + FileUtils.byteCountToDisplaySize(repoData.getTotalSizeImages()) + ".");
			} else {
				System.out.println("Repositorio " + repoData.getName() + " não possui nenhuma imagem!");
			}
		});

		long totalSizeImagesOfAllRepos = 0;
		for(RepoData r : repoNameImageDetailMap.keySet()) {
			if (r.getTotalSizeImages() != null) {
				totalSizeImagesOfAllRepos += r.getTotalSizeImages();
			}
		}
		System.out.println("O ECR possui atualmente " + FileUtils.byteCountToDisplaySize(totalSizeImagesOfAllRepos) + " de espaço armazenado.");
	}
}