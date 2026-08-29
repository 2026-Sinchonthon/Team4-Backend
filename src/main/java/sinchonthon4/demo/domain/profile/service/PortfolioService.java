package sinchonthon4.demo.domain.profile.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sinchonthon4.demo.domain.profile.dto.PortfolioRequest;
import sinchonthon4.demo.domain.profile.dto.PortfolioResponse;
import sinchonthon4.demo.domain.profile.entity.Portfolio;
import sinchonthon4.demo.domain.profile.repository.PortfolioRepository;
import sinchonthon4.demo.domain.user.entity.User;
import sinchonthon4.demo.domain.user.repository.UserRepository;
import sinchonthon4.demo.global.exception.BusinessException;
import sinchonthon4.demo.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional
    public PortfolioResponse create(Long currentUserId, PortfolioRequest request) {
        User user = getUser(currentUserId);
        Portfolio portfolio = Portfolio.create(user, request.imageUrl());
        return PortfolioResponse.from(portfolioRepository.save(portfolio));
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getMyPortfolios(Long currentUserId) {
        getUser(currentUserId);
        return portfolioRepository.findAllByUser_IdOrderByCreatedAtDescIdDesc(currentUserId).stream()
                .map(PortfolioResponse::from)
                .toList();
    }

    @Transactional
    public PortfolioResponse update(Long currentUserId, Long portfolioId, PortfolioRequest request) {
        getUser(currentUserId);
        Portfolio portfolio = getPortfolio(portfolioId);
        validateOwner(portfolio, currentUserId);

        portfolio.updateImageUrl(request.imageUrl());
        return PortfolioResponse.from(portfolio);
    }

    @Transactional
    public void delete(Long currentUserId, Long portfolioId) {
        getUser(currentUserId);
        Portfolio portfolio = getPortfolio(portfolioId);
        validateOwner(portfolio, currentUserId);
        portfolioRepository.delete(portfolio);
    }

    private User getUser(Long currentUserId) {
        return userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Portfolio getPortfolio(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private void validateOwner(Portfolio portfolio, Long currentUserId) {
        if (!portfolio.getUser().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.PORTFOLIO_FORBIDDEN);
        }
    }
}
