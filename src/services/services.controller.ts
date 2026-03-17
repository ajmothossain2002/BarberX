import {
    Controller,
    Get,
    Post,
    Body,
    Param,
    Patch,
    Delete,
    UseGuards,
    Request,
} from '@nestjs/common';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { ServicesService } from './services.service';
import { CreateServiceDto, UpdateServiceDto } from './dto';

@Controller('services')
export class ServicesController {
    constructor(private servicesService: ServicesService) { }

    @Post('shop/:shopId')
    @UseGuards(JwtAuthGuard)
    create(
        @Param('shopId') shopId: string,
        @Request() req,
        @Body() dto: CreateServiceDto,
    ) {
        return this.servicesService.create(shopId, req.user.userId, dto);
    }

    @Get('shop/:shopId')
    findAll(@Param('shopId') shopId: string) {
        return this.servicesService.findAll(shopId);
    }

    @Get(':id')
    findOne(@Param('id') id: string) {
        return this.servicesService.findOne(id);
    }

    @Patch(':id')
    @UseGuards(JwtAuthGuard)
    update(@Param('id') id: string, @Request() req, @Body() dto: UpdateServiceDto) {
        return this.servicesService.update(id, req.user.userId, dto);
    }

    @Delete(':id')
    @UseGuards(JwtAuthGuard)
    delete(@Param('id') id: string, @Request() req) {
        return this.servicesService.delete(id, req.user.userId);
    }
}
