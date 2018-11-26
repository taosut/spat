import {apiUtils, teamApi} from '../../rest-api';

export const LOAD_TEAMS = 'LOAD_TEAMS';
export const SHOW_TEAMS = 'SHOW_TEAMS';
export const HANDLE_TEAM_LIST_LOAD_ERROR = 'HANDLE_TEAM_LIST_LOAD_ERROR';
export const SHOW_TEAM_MODAL = 'SHOW_TEAM_MODAL';
export const SHOW_TEAM_MODAL_ERROR = 'SHOW_TEAM_MODAL_ERROR';
export const HIDE_TEAM_MODAL = 'HIDE_TEAM_MODAL';
export const ADD_TEAM = 'ADD_TEAM';
export const EDIT_TEAM = 'EDIT_TEAM';
export const DELETE_TEAM = 'DELETE_TEAM';

export const loadTeams = () => async dispatch => {
  dispatch({type: LOAD_TEAMS, loading: true});
  try {
    const teams = await teamApi.fetchTeams();
    dispatch({type: SHOW_TEAMS, list: teams});
  } catch (error) {
    apiUtils.logError(error);
    dispatch({type: HANDLE_TEAM_LIST_LOAD_ERROR});
  }
};

export const showTeamModal = (mode, team, submitCallback) => dispatch => {
  dispatch({type: SHOW_TEAM_MODAL, mode: mode, team: team, submitCallback: submitCallback});
};

export const hideTeamModal = () => dispatch => {
  dispatch({type: HIDE_TEAM_MODAL});
};

export const addTeam = (team) => async dispatch => {
  try {
    team = await teamApi.addTeam(team);
    dispatch({type: ADD_TEAM, team: team});
  } catch (error) {
    apiUtils.logError(error);
    dispatch({
      type: SHOW_TEAM_MODAL_ERROR,
      message: `Unable to add ${team.name}.  Please try again later.`
    });
  }
};

export const editTeam = (team) => async dispatch => {
  try {
    team = await teamApi.editTeam(team);
    dispatch({type: EDIT_TEAM, team: team});
  } catch (error) {
    apiUtils.logError(error);
    dispatch({
      type: SHOW_TEAM_MODAL_ERROR,
      message: `Unable to edit ${team.name}.  Please try again later.`
    });
  }
};

export const deleteTeam = (team) => async dispatch => {
  try {
    await teamApi.deleteTeam(team);
    dispatch({type: DELETE_TEAM, team: team});
  } catch (error) {
    apiUtils.logError(error);
    dispatch({
      type: SHOW_TEAM_MODAL_ERROR,
      message: `Unable to delete ${team.name}.  Please try again later.`
    });
  }
};