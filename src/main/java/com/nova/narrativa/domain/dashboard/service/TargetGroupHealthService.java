package com.nova.narrativa.domain.dashboard.service;

import com.nova.narrativa.domain.dashboard.dto.TargetHealthResponse;
import com.nova.narrativa.domain.dashboard.util.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.elasticloadbalancingv2.ElasticLoadBalancingV2Client;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import software.amazon.awssdk.services.elasticloadbalancingv2.model.DescribeTargetHealthResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TargetGroupHealthService {

    private final ElasticLoadBalancingV2Client elbClient;
    private final AwsProperties awsProperties;

    public Map<String, List<TargetHealthResponse>> getAllTargetGroupsHealth() {
        return awsProperties.getTargetGroups().stream()
                .collect(Collectors.toMap(
                        this::getTargetGroupName,
                        this::getTargetGroupHealth
                ));
    }

    private List<TargetHealthResponse> getTargetGroupHealth(String targetGroupArn) {
        DescribeTargetHealthRequest request = DescribeTargetHealthRequest.builder()
                .targetGroupArn(targetGroupArn)
                .build();

        DescribeTargetHealthResponse response = elbClient.describeTargetHealth(request);
        String targetGroupName = getTargetGroupName(targetGroupArn);

        return response.targetHealthDescriptions().stream()
                .map(description -> new TargetHealthResponse(
                        parseTargetId(description.target().id()), // 사용자 친화적인 이름으로 변환
                        description.targetHealth().stateAsString(),
                        description.targetHealth().reasonAsString(),
                        description.targetHealth().description(),
                        targetGroupName
                ))
                .collect(Collectors.toList());
    }

    private String parseTargetId(String targetId) {
        return switch (targetId) {
            case "i-0868578459da97445" -> "Narrativa-Frontend";
            case "i-0e663b64d94f084f4" -> "Narrativa-Admin";
            case "i-0634c6fefd1c18aee" -> "Narrativa-ML";
            default -> "Narrativa-Backend";
        };
    }

    private String getTargetGroupName(String arn) {
        return arn.substring(arn.lastIndexOf("/") + 1);
    }
}