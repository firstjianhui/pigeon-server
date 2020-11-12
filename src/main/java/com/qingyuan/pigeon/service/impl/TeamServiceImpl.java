package com.qingyuan.pigeon.service.impl;

import com.qingyuan.pigeon.enums.ResponseResultEnum;
import com.qingyuan.pigeon.mapper.TeamMapper;
import com.qingyuan.pigeon.mapper.TeamMemberMapper;
import com.qingyuan.pigeon.mapper.UserMessageMapper;
import com.qingyuan.pigeon.pojo.Team;
import com.qingyuan.pigeon.pojo.User;
import com.qingyuan.pigeon.service.TeamService;
import com.qingyuan.pigeon.utils.UniversalResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 团队相关接口
 * @author 24605
 */
@Service
@Slf4j
public class TeamServiceImpl implements TeamService {

    /**
     * 存储团队头像的文件夹,团队头像命名请用teamId.png
     */
    private static final String TEAM_AVATAR_DIR_PATH = "/a-pigeon/image-pigeon/team-avatar/";
    /**
     * 团队头像的访问地址,直接在该URL后面加上teamId.png就可以
     */
    private static  final String USER_IMAGE_URL = "https://minimalist.net.cn/image-pigeon/team-avatar/";

    @Resource
    private TeamMapper teamMapper;
    @Resource
    private TeamMemberMapper teamMemberMapper;
    @Resource
    private UserMessageMapper userMessageMapper;

    @Override
    public UniversalResponseBody<Team> createTeam(Team team, MultipartFile multipartFile) {
        int affectedRow = teamMapper.createTeam(team);
        if (affectedRow < 1) {
            return new UniversalResponseBody<>(ResponseResultEnum.FAILED.getCode(), ResponseResultEnum.FAILED.getMsg());
        }

        String teamFileName = team.getTeamId() + ".png";
        String filePath = TEAM_AVATAR_DIR_PATH + teamFileName;
        log.info("团队上传头像，路径为", filePath);

        try {
            multipartFile.transferTo(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return new UniversalResponseBody<>(ResponseResultEnum.FAILED.getCode(), ResponseResultEnum.FAILED.getMsg());
        }

        String imageUrl = USER_IMAGE_URL + teamFileName;
        team.setTeamAvatarUrl(imageUrl);
        int i = teamMapper.updateTeamAvatar(team.getTeamId(), imageUrl);
        if (i > 0) {
            return new UniversalResponseBody<>(ResponseResultEnum.SUCCESS.getCode(), ResponseResultEnum.SUCCESS.getMsg(), team);
        }

        return new UniversalResponseBody<>(ResponseResultEnum.FAILED.getCode(), ResponseResultEnum.FAILED.getMsg());
    }

    @Override
    @Transactional
    public UniversalResponseBody<Team> applyTeam(Integer teamId, Integer userId) {
        // 判断该用户是否是该团队成员
        int count = teamMemberMapper.getUserNumberFromTeam(teamId, userId);
        if (count > 0) {
            return new UniversalResponseBody<>(ResponseResultEnum.TEAM_MEMBER_IS_EXISTED.getCode(), ResponseResultEnum.TEAM_MEMBER_IS_EXISTED.getMsg());
        }

        // 判断teamId的team的申请人数是否达到了上限
        Team team = teamMapper.getTeamById(teamId);
        if (team.getMemberCountMax() <= team.getTeamApplyCount()) {
            return new UniversalResponseBody<>(ResponseResultEnum.TEAM_MEMBER_REACH_MAX.getCode(), ResponseResultEnum.TEAM_MEMBER_REACH_MAX.getMsg());
        }

        // 把该用户加入到该team中
        int affectedRow = teamMemberMapper.addUser(teamId, userId);
        team.setTeamApplyCount(team.getTeamApplyCount() + 1);
        teamMapper.updateTeamApplyCount(team.getTeamId(), team.getTeamApplyCount());
        if (affectedRow > 0) {
            return new UniversalResponseBody<>(ResponseResultEnum.SUCCESS.getCode(), ResponseResultEnum.SUCCESS.getMsg(), team);
        }

        return new UniversalResponseBody<>(ResponseResultEnum.FAILED.getCode(), ResponseResultEnum.FAILED.getMsg());
    }

    @Override
    public UniversalResponseBody<List<User>> getTeamUsers(Integer teamId) {
        // 通过teamId来查找成员
        List<User> users = userMessageMapper.getUsersByTeamId(teamId);
        if (users != null){
            return new UniversalResponseBody<>(ResponseResultEnum.SUCCESS.getCode(), ResponseResultEnum.SUCCESS.getMsg(), users);
        }
        return new UniversalResponseBody<>(ResponseResultEnum.RESULT_IS_NULL.getCode(),ResponseResultEnum.RESULT_IS_NULL.getMsg(),null);
    }

    @Override
    public UniversalResponseBody<List<Team>> getTeamsByType(Integer userId, String activityType) {
        List<Integer> teamIds = teamMapper.getTeamsByUserId(userId);
        if (teamIds == null || teamIds.isEmpty()){
            return new UniversalResponseBody<>(ResponseResultEnum.USER_NOT_HAVE_TEAM.getCode(),ResponseResultEnum.USER_NOT_HAVE_TEAM.getMsg(),null);
        }
        List<Team> teams = new LinkedList<>();
        for (Integer teamId:
             teamIds) {
            teams.add(teamMapper.getTeamsByIdType(teamId, activityType));
        }
        return new UniversalResponseBody<>(ResponseResultEnum.SUCCESS.getCode(),ResponseResultEnum.SUCCESS.getMsg(), teams);
    }

    @Override
    public UniversalResponseBody<Team> getTeamByTeamId(Integer teamId) {
        //根据团队id查询团队
        Team team = teamMapper.getTeamById(teamId);
        if (team != null){
            return new UniversalResponseBody<>(ResponseResultEnum.SUCCESS.getCode(),ResponseResultEnum.SUCCESS.getMsg(), team);
        }
        return new UniversalResponseBody<>(ResponseResultEnum.FAILED.getCode(),ResponseResultEnum.FAILED.getMsg());
    }

    @Override
    public UniversalResponseBody<List<Team>> getTeamsByUserId(Integer userId) {
        //查询用户的团队
        List<Integer> teamIds = teamMapper.getTeamsByUserId(userId);
        if (teamIds == null || teamIds.isEmpty()){
            return new UniversalResponseBody<>(ResponseResultEnum.USER_NOT_HAVE_TEAM.getCode(),ResponseResultEnum.USER_NOT_HAVE_TEAM.getMsg(),null);
        }
        List<Team> teams = new LinkedList<>();
        for (Integer teamId: teamIds
             ) {
            teams.add(teamMapper.getTeamById(teamId));
        }
        //如果进行到这一步，则说明teamIds不为空，肯定能查出对应的团队
        //所以不用考虑返回结果为空的情况
        return new UniversalResponseBody<>(ResponseResultEnum.SUCCESS.getCode(),ResponseResultEnum.SUCCESS.getMsg(), teams);
    }
}
