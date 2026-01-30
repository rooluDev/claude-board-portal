import router from '@/router'

// ErrorCommand 추상 클래스
class ErrorCommand {
  constructor(error) {
    this.error = error
  }

  execute() {
    throw new Error('execute() must be implemented')
  }
}

// 구체적인 Command 클래스들
class BoardNotFoundCommand extends ErrorCommand {
  execute() {
    alert('존재하지 않는 게시물입니다.')
    router.push({ name: 'Main' })
  }
}

class NotLoggedInCommand extends ErrorCommand {
  execute() {
    router.push({ name: 'Error' })
  }
}

class IllegalFileDataCommand extends ErrorCommand {
  execute() {
    alert('잘못된 파일 데이터입니다.')
    router.push({ name: 'Error' })
  }
}

class LoginFailCommand extends ErrorCommand {
  execute() {
    console.log('로그인 실패:', this.error.response?.data?.message)
  }
}

class NotMyBoardCommand extends ErrorCommand {
  execute() {
    console.log('본인의 게시물이 아닙니다.')
  }
}

class DefaultErrorCommand extends ErrorCommand {
  execute() {
    console.error('에러 발생:', this.error)
    alert('오류가 발생했습니다.')
  }
}

// Factory
export class ErrorCommandFactory {
  static createCommand(error) {
    if (!error.response || !error.response.data) {
      return new DefaultErrorCommand(error)
    }

    const errorCode = error.response.data.code

    switch (errorCode) {
      case 'A001': return new BoardNotFoundCommand(error)
      case 'A002': return new BoardNotFoundCommand(error)
      case 'A003': return new BoardNotFoundCommand(error)
      case 'A005': return new NotLoggedInCommand(error)
      case 'A006': return new NotMyBoardCommand(error)
      case 'A007': return new LoginFailCommand(error)
      case 'A008': return new IllegalFileDataCommand(error)
      case 'A009': return new LoginFailCommand(error)
      case 'A013': return new LoginFailCommand(error)
      case 'A014': return new LoginFailCommand(error)
      default: return new DefaultErrorCommand(error)
    }
  }
}
