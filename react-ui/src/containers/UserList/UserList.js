import React, {Component}                  from 'react';
import {connect}                           from 'react-redux';
import {Card, CardBody, CardHeader, Table} from 'reactstrap';
import {FontAwesomeIcon}                   from '@fortawesome/react-fontawesome';

import './UserList.css';
import * as userActions                    from '../../redux/actions/user';

class UserList extends Component {
  state = {
    modalIsOpen: false
  };

  openModal = () => {
    this.setState({
      modalIsOpen: !this.state.modalIsOpen
    });
  };

  closeModal = () => {
    this.setState({modalIsOpen: !this.state.modalIsOpen});
  };

  displayAccountStatus = (user) => {
    const accountStatus = [];
    if (user.enabled) {
      accountStatus.push('Enabled');
    } else {
      accountStatus.push('Disabled');
    }
    if (!user.accountNonExpired) {
      accountStatus.push('Expired');
    }
    if (!user.accountNonLocked) {
      accountStatus.push('Locked');
    }
    if (!user.credentialsNonExpired) {
      accountStatus.push('Credentials Expired');
    }
    return accountStatus.join(', ');
  };

  displayRoles = (user) => {
    return user.roles.map(item => item.name).join(', ');
  };

  componentDidMount = () => {
    this.props.loadUsers();
  };

  render() {
    let content;
    if (this.props.loading) {
      content = (<h1>Loading...</h1>);
    } else if (this.props.showError) {
      content = (<h1>Error Loading Users</h1>);
    } else {
      content = (
        <React.Fragment>
          <Table striped bordered hover>
            <thead>
              <tr>
                <th className="action-column"/>
                <th className="action-column"/>
                <th>Username</th>
                <th>Account Status</th>
                <th>Roles</th>
              </tr>
            </thead>
            <tbody>
              {this.props.users.map(user => {
                return (
                  <tr key={user.username}>
                    <td className="action-column"><FontAwesomeIcon icon="pencil-alt"/></td>
                    <td className="action-column"><FontAwesomeIcon icon="trash-alt"/></td>
                    <td>{user.username}</td>
                    <td>{this.displayAccountStatus(user)}</td>
                    <td>{this.displayRoles(user)}</td>
                  </tr>
                );
              })}
            </tbody>
          </Table>
          {/*<UserModal*/}
          {/*open={this.state.modalIsOpen}*/}
          {/*title={this.state.modalTitle}*/}
          {/*cancelModal={this.closeModal}*/}
          {/*/>*/}
        </React.Fragment>
      );
    }

    return (
      <Card className="UserList m-2">
        <CardHeader>Users</CardHeader>
        <CardBody>
          {content}
        </CardBody>
      </Card>
    );
  }
}

const mapStateToProps = (state) => {
  return {
    loggedInUser: state.auth.loggedInUser,
    loading: state.users.loading,
    showError: state.users.showError,
    users: state.users.list
  };
};

const mapDispatchToProps = (dispatch) => {
  return {
    loadUsers: () => dispatch(userActions.loadUsers())
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(UserList);